package com.example.desafio_6_sockets.Controller;

import android.net.Uri;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;

import com.example.desafio_6_sockets.View.MainActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

    //===============================================================================< Class >=================================================================================//

public class ChatClient extends Thread {
    //private static final String SERVER_IP = "192.168.0.108"; // Endereço IP do servidor de casa:
    private static final String SERVER_IP = "192.168.7.214"; // Endereço de IP do servidor da Imply:
    private static final int SERVER_PORT = 4000; // Porta do servidor
    private static final String TAG = "Gabes";
    private BufferedWriter out;
    private BufferedReader in;
    private Socket clientSocket;
    private MainActivity mainActivity;

    //================================================================================< Main >==================================================================================//

    public ChatClient (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket(SERVER_IP, SERVER_PORT);

            // Para enviar mensgagens para o servidor
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

            // Receber resposta do servidor
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            receiveMessageLoop(clientSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //====================================================================< Receber Mensagem >=================================================================================//

    public void receiveMessageLoop (Socket clientSocket) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        String serverResponse = in.readLine();
                        if (serverResponse.startsWith("image")) {
                            String codeFile = "";

                            while (!serverResponse.endsWith("END_OF_IMAGE")){
                                serverResponse = in.readLine();
                                codeFile += serverResponse + "\n";
                            }
                            Uri decodedFile = decodeBase64ToUri(codeFile);
                            Log.i(TAG, "codificando a imagem: " + decodedFile);

                            mainActivity.showReceiveImage(decodedFile);
                        } else {
                            mainActivity.showReceiveMessage(serverResponse);
                        }

                    } catch (IOException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    //=====================================================================< Enviar Mensagem >=================================================================================//

    public void sendMessage(String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    Log.i(TAG, "Mensagem a ser enviada: " + message);
                    out.write(message);
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    //=======================================================================< Enviar Imagem >=================================================================================//

    public void sendImage(String base64Image){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write("image " + base64Image + " END_OF_IMAGE");
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    //======================================================================< Enviar Arquivo >=================================================================================//

    public void sendFile(String base64Image){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write("arquivo " + base64Image);
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    //========================================================================< Enviar Audio >=================================================================================//

    public void sendAudio(String base64Image){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write("audio " + base64Image);
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    //===============================================================================< Close >=================================================================================//

    public void close(){
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public Uri decodeBase64ToUri (String fileBase64) {

        String type = "";
        if (fileBase64.startsWith("image")) {
            type = "image";
            fileBase64 = fileBase64.replace("image ", "");
            fileBase64 = fileBase64.replace("END_OF_IMAGE", "");
        }

        Log.i(TAG, "oi: " + fileBase64);


        String fullImage = "data:image/png;base64," + fileBase64;
        Uri imageUri = Uri.parse(fullImage);
        Log.i(TAG, "return: " + imageUri);

        return imageUri;
    }
}

//=====================================================================================< Fim >=================================================================================//
