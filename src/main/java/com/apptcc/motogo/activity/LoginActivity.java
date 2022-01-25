package com.apptcc.motogo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apptcc.motogo.R;
import com.apptcc.motogo.config.ConfiguracaoFirebase;
import com.apptcc.motogo.helper.UsuarioFirebase;
import com.apptcc.motogo.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inicializar componentes
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);



    }

    public void validarLoginUsuario(View view) {

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoEmail.isEmpty()) {

            if (!textoSenha.isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);
            } else {
                Toast.makeText(LoginActivity.this,
                        "Preencha a senha!",
                        Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(LoginActivity.this,
                    "Preencha o email!",
                    Toast.LENGTH_SHORT).show();
            }
    }

        public void logarUsuario(Usuario usuario){

            autenticacao = ConfiguracaoFirebase.getFirabaseAutenticacao();
            autenticacao.signInWithEmailAndPassword(
                    usuario.getEmail(), usuario.getSenha()
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        //Verificar o tipo de usuário logado
                        //"Mototaxi" / "Passageiro"
                        UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);

                       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Seja bem vindo " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                        if (user != null) {// Verifica se o usuario está logado
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }

                    }else{
                        String excecao = "";
                        try {
                            throw task.getException();
                        }catch( FirebaseAuthInvalidUserException e ){
                            excecao = "Usuário não está cadastrado.";
                        }catch( FirebaseAuthInvalidCredentialsException e ){
                            excecao = "E-mail e senha não correspondem a um usuario cadastro";
                        } catch (Exception e) {
                            excecao = "Erro ao logar ao usuario: " + e.getMessage();
                            e.printStackTrace();
                        }
                        Toast.makeText(LoginActivity.this,
                                "Erro: " + excecao,
                                Toast.LENGTH_LONG).show();


                    }


                }
            });

        }

    }


