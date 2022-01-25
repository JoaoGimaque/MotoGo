package com.apptcc.motogo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.apptcc.motogo.R;
import com.apptcc.motogo.helper.Local;
import com.apptcc.motogo.model.Requisicao;
import com.apptcc.motogo.model.Usuario;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Requisicoesadapter extends RecyclerView.Adapter<Requisicoesadapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario mototaxista;


    public Requisicoesadapter(List<Requisicao> requisicoes, Context context, Usuario mototaxista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.mototaxista = mototaxista;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent, false);
        return new MyViewHolder( item ) ;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Requisicao requisicao = requisicoes.get( position );
        Usuario passageiro = requisicao.getPassageiro();

        holder.nome.setText( passageiro.getNome() );

        if(mototaxista!= null){

            LatLng localPassageiro = new LatLng(
                    Double.parseDouble(passageiro.getLatitude()),
                    Double.parseDouble(passageiro.getLongitude())
            );

           LatLng localMotaxista = new LatLng(
                   Double.parseDouble(mototaxista.getLatitude()),
                   Double.parseDouble(mototaxista.getLongitude())
           );

            float distancia = Local.calcularDistancia(localPassageiro, localMotaxista);
            String distanciaFormatada = Local.formatarDistancia(distancia);
            holder.distancia.setText(distanciaFormatada + "- aproximadamente");

        }
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, distancia;
        public MyViewHolder(View itemView){
            super(itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }

    }

}
