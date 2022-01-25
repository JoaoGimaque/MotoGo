package com.apptcc.motogo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.apptcc.motogo.R;
import com.apptcc.motogo.config.ConfiguracaoFirebase;
import com.apptcc.motogo.databinding.ActivityPassageiroBinding;
import com.apptcc.motogo.helper.UsuarioFirebase;
import com.apptcc.motogo.model.Destino;
import com.apptcc.motogo.model.Requisicao;
import com.apptcc.motogo.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*
    *
    * Lat/lon Destino: 2.808144905706448, -60.72517179337198
    * Lat/lon Passageiro: 2.8099719485714174, -60.72745166080468
    * Lat/lon mototaxista(a caminho):
    *      inicial: 2.8190269029757395, -60.72854601426129
    *       intermediaria: 2.8147941335023736, -60.72818123384346
    *           final: 2.8103041425313515, -60.727473118477434
    * */

    //componentes
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button buttonChamarMoto;

    private ActivityPassageiroBinding binding;
    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean cancelarMoto = false;
    private DatabaseReference firebaseRef;
    private DatabaseReference requisicoes;
    private Requisicao requisicao;
    private Usuario passageiro;
    private String statusRequisicao;
    private Destino destino;
    private Marker marcadorMototaxista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private Usuario mototaxista;
    private LatLng localMototaxista;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializarComponentes();


       verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = FirebaseDatabase.getInstance("https://moto-go-eee1f-default-rtdb.firebaseio.com/").getReference();
        requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo( usuarioLogado.getId() );

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for( DataSnapshot ds: dataSnapshot.getChildren() ){
                    lista.add( ds.getValue( Requisicao.class ) );
                }

                Collections.reverse(lista);
                if( lista!= null && lista.size()>0 ){
                    requisicao = lista.get(0);

                    if(requisicao != null){
                        if( !requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADA) ) {
                            passageiro = requisicao.getPassageiro();
                            localPassageiro = new LatLng(
                                    Double.parseDouble(passageiro.getLatitude()),
                                    Double.parseDouble(passageiro.getLongitude())
                            );
                            statusRequisicao = requisicao.getStatus();
                            destino = requisicao.getDestino();
                            if (requisicao.getMototaxista() != null) {
                                mototaxista = requisicao.getMototaxista();
                                localMototaxista = new LatLng(
                                        Double.parseDouble(mototaxista.getLatitude()),
                                        Double.parseDouble(mototaxista.getLongitude())
                                );
                            }
                            alteraInterfaceStatusRequisicao(statusRequisicao);
                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void alteraInterfaceStatusRequisicao(String status){

        if(status != null && !status.isEmpty()) {
            cancelarMoto = false;
            switch (status) {
                case Requisicao.STATUS_AGUARDANDO:
                    requisicaoAguardando();
                    break;
                case Requisicao.STATUS_A_CAMINHO:
                    requisicaoACaminho();
                    break;
                case Requisicao.STATUS_VIAGEM:
                    requisicaoViagem();
                    break;
                case Requisicao.STATUS_FINALIZADA:
                    requisicaoFinalizada();
                    break;
                case Requisicao.STATUS_CANCELADA:
                    requisicaoCancelada();
                    break;

            }
        }else {
            //Adiciona marcador passageiro
            adicionaMarcadorPassageiro(localPassageiro, "Seu local");
            centralizarMarcador(localPassageiro);
        }

    }

    private void requisicaoCancelada(){

        linearLayoutDestino.setVisibility( View.VISIBLE );
        buttonChamarMoto.setText("Chamar mototaxixta");
        cancelarMoto= false;

    }

    private void requisicaoAguardando(){

        linearLayoutDestino.setVisibility( View.GONE );
        buttonChamarMoto.setText("Cancelar mototaxista");
        cancelarMoto = true;

        //Adiciona marcador passageiro
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());
        centralizarMarcador(localPassageiro);

    }

    private void requisicaoACaminho(){

        linearLayoutDestino.setVisibility( View.GONE );
        buttonChamarMoto.setText("Mototaxista a caminho");
        buttonChamarMoto.setEnabled(false);

        Toast.makeText(this,
                "Clique no icone do capacete para ver o nome do mototaxista",
                Toast.LENGTH_LONG).show();

        //Adiciona marcador passageiro
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

        //Adiciona marcador motorista
        adicionaMarcadorMototaxista(localMototaxista, mototaxista.getNome());

        //Centralizar passageiro / motorista
        centralizarDoisMarcadores(marcadorMototaxista, marcadorPassageiro);

    }

    private void requisicaoViagem(){

        linearLayoutDestino.setVisibility( View.GONE );
        buttonChamarMoto.setText("A caminho do destino");
        buttonChamarMoto.setEnabled(false);

        //Adiciona marcador motorista
        adicionaMarcadorMototaxista(localMototaxista, mototaxista.getNome());

        //Adiciona marcador de destino
        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionaMarcadorDestino(localDestino, "Destino");

        //Centraliza marcadores motorista / destino
        centralizarDoisMarcadores(marcadorMototaxista, marcadorDestino);

    }

    private void requisicaoFinalizada(){

        linearLayoutDestino.setVisibility( View.GONE );
        buttonChamarMoto.setEnabled(false);

        //Adiciona marcador de destino
        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionaMarcadorDestino(localDestino, "Destino");
        centralizarMarcador(localDestino);



        buttonChamarMoto.setText("Corrida finalizada" );

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Total da viagem")
                .setMessage("Sua viagem ficou no valor de R$10,00")
                .setCancelable(false)
                .setNegativeButton("Encerrar viagem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                        requisicao.atualizarStatus();

                        finish();
                        startActivity(new Intent(getIntent()));

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo){

        if( marcadorPassageiro != null )
            marcadorPassageiro.remove();

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );

    }

    private void adicionaMarcadorMototaxista(LatLng localizacao, String titulo){

        if( marcadorMototaxista != null )
            marcadorMototaxista.remove();

        marcadorMototaxista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mototaxi))
        );

    }

    private void adicionaMarcadorDestino(LatLng localizacao, String titulo){

        if( marcadorPassageiro != null )
            marcadorPassageiro.remove();

        if( marcadorDestino != null )
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );

    }

    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include( marcador1.getPosition() );
        builder.include( marcador2.getPosition() );

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura,altura,espacoInterno)
        );

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //recuperando a localização do usuario
        recuperarLocalizacaoUsuairo();


    }

    public void chamarMoto(View view){

        if ( cancelarMoto){
            //Cancelar a requisição
            requisicao.setStatus(Requisicao.STATUS_CANCELADA);
            requisicao.atualizarStatus();

        }else {

            String enderecoDestino = editDestino.getText().toString();

            if( !enderecoDestino.equals("") || enderecoDestino != null ){

                Address addressDestino = recuperarEndereco( enderecoDestino );
                if( addressDestino != null ){

                    final Destino destino = new Destino();
                    destino.setCidade( addressDestino.getAdminArea() );
                    destino.setCep( addressDestino.getPostalCode() );
                    destino.setBairro( addressDestino.getSubLocality() );
                    destino.setRua( addressDestino.getThoroughfare() );
                    destino.setNumero( addressDestino.getFeatureName() );
                    destino.setLatitude( String.valueOf(addressDestino.getLatitude()) );
                    destino.setLongitude( String.valueOf(addressDestino.getLongitude()) );

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append( "\nRua: " + destino.getRua() );
                    mensagem.append( "\nBairro: " + destino.getBairro() );
                    mensagem.append( "\nNúmero: " + destino.getNumero() );
                    mensagem.append( "\nCep: " + destino.getCep() );
                    mensagem.append("\nA corrida tem o valor fixo de R$ 10,00");

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereco!")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //salvar requisição
                                    salvarRequisicao( destino );

                                }
                            }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }

            }else {
                Toast.makeText(this,
                        "Informe o endereço de destino!",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();

        requisicao.setDestino( destino );

        Usuario usuarioPassageiro= UsuarioFirebase.getDadosUsuarioLogado();

        usuarioPassageiro.setLatitude( String.valueOf( localPassageiro.latitude ) );
        usuarioPassageiro.setLongitude( String.valueOf( localPassageiro.longitude ) );
        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus( Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();


        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarMoto.setText("Cancelar Mototaxi");
    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if( listaEnderecos != null && listaEnderecos.size() > 0 ){
                Address address = listaEnderecos.get(0);

                return address;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private void recuperarLocalizacaoUsuairo() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                //atualziar geofire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localPassageiro)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );

                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localPassageiro, 20)
                );

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //Solicitar atulizações de lozalização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){


        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle("Iniciar uma viagem");
        setSupportActionBar(binding.toolbar);

        //inicializar componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonChamarMoto = findViewById(R.id.buttonChamarMoto);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirabaseAutenticacao();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
}