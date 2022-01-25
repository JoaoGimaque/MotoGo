package com.apptcc.motogo.model;




import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.apptcc.motogo.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Requisicao {


    protected String id;
    private String status;
    private Usuario passageiro;
    private Usuario mototaxista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_VIAGEM= "viagem";
    public static final String STATUS_FINALIZADA = "finalizada";
    public static final String STATUS_ENCERRADA = "encerrada";
    public static final String STATUS_CANCELADA = "cancelada";


    public Requisicao() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();

        setId( idRequisicao );
        requisicoes.child(getId()).setValue(this);
    }

    public void atualizar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("mototaxista", getMototaxista());
        objeto.put("status", getStatus());

       requisicao.updateChildren(objeto);



    }
    public void atualizarStatus(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren( objeto );

    }

    public void atualizarLocalizacaoMototaxista(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef
                .child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId())
                .child("mototaxista");

        Map objeto = new HashMap();
        objeto.put("latitude", getMototaxista().getLatitude() );
        objeto.put("longitude", getMototaxista().getLongitude());

        requisicao.updateChildren( objeto );

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMototaxista() {
        return mototaxista;
    }

    public void setMototaxista(Usuario mototaxista) {
        this.mototaxista = mototaxista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
