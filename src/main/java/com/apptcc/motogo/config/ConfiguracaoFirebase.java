package com.apptcc.motogo.config;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ConfiguracaoFirebase {
    private static DatabaseReference database;
    private static FirebaseAuth auth;

    //retorna a instancia do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase(){

        if (database == null){
            database = FirebaseDatabase.getInstance("https://moto-go-eee1f-default-rtdb.firebaseio.com/").getReference();
        }

        return database;
    }

    //retorna a instancia do FirebaseAuth
    public static FirebaseAuth getFirabaseAutenticacao(){

        if (auth == null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }



}
