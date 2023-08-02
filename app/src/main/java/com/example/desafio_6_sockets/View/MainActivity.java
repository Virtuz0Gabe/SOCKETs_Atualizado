package com.example.desafio_6_sockets.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.desafio_6_sockets.Controller.ChatClient;
import com.example.desafio_6_sockets.R;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

//==========================================================================< Main >=================================================================================//

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Gabes";
    private ChatClient chatClient;
    ImageButton imageButtonSend;
    EditText editTextMessage;
    ImageButton imageButtonAttach;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ChatClient socketClient;

    //====================================================================< On Create >=================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButtonSend = findViewById(R.id.imageButtonSend);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageButtonAttach = findViewById(R.id.imageButtonAttach);

        // Inicia a conexão com o servidor Socket quando a Activity for criada
        socketClient = new ChatClient(this);
        socketClient.start();

        //==========================================================< Enviar Mensagem Layout >=================================================================================//

        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = String.valueOf(editTextMessage.getText());
                editTextMessage.setText("");
                socketClient.sendMessage(message);

                LinearLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_own_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);
            }
        });

    //=====================================================================< Verificar Tipo >=================================================================================//

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            Uri selectedFileUri = result.getData().getData();

                            // Verifica se o arquivo selecionado é uma imagem
                            if (selectedFileUri != null && getContentResolver().getType(selectedFileUri).startsWith("image/")) {
                                try {
                                    String imageBase64 = convertFileToBase64(selectedFileUri);
                                    socketClient.sendImage(imageBase64);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Verifica se o arquivo selecionado é um arquivo
                            if (selectedFileUri != null && getContentResolver().getType(selectedFileUri).startsWith("application/")) {
                                try {
                                    String fileBase64 = convertFileToBase64(selectedFileUri);
                                    socketClient.sendFile(fileBase64);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Verifica se o arquivo selecionado é um áudio
                            if (selectedFileUri != null && getContentResolver().getType(selectedFileUri).startsWith("audio/")) {
                                try {
                                    String audioBase64 = convertFileToBase64(selectedFileUri);
                                    socketClient.sendAudio(audioBase64);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        //================================================================< AnexarBTN OnClick >=================================================================================//

        imageButtonAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View customLayout = getLayoutInflater().inflate(R.layout.alert_dialog_custom, null);
                builder.setView(customLayout);

                TextView titleTextView = customLayout.findViewById(R.id.dialog_title);
                titleTextView.setText("Escolha uma opção:");


                // Botão "Galeria"
                LinearLayout buttonGaleria = customLayout.findViewById(R.id.button_galeria);
                buttonGaleria.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGalleryIMG();
                    }
                });


                // Botão "Enviar Arquivo"
                LinearLayout buttonEnviarArquivo = customLayout.findViewById(R.id.button_enviar_arquivo);
                buttonEnviarArquivo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGalleryFiles();
                    }
                });


                // Botão "Enviar Áudio"
                LinearLayout buttonEnviarAudio = customLayout.findViewById(R.id.button_enviar_audio);
                buttonEnviarAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGalleryAudio();
                    }
                });

                builder.create().show();
            }
        });
    }

    //public void onGaleriaClick(View view) {openGalleryAudio();}
    //public void onEnviarArquivoClick(View view) {openGalleryAudio();}
    //public void onEnviarAudioClick(View view) {openGalleryAudio();}

    //===============================================================< Abrir Galeria Imagens>=================================================================================//

    private void openGalleryIMG() {
        Log.d(TAG, "Botão de anexo clicado. Abrindo a galeria de imagens.");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //==============================================================< Abrir Galeria Arquivos >=================================================================================//

    // Método para abrir a galeria de arquivos
    private void openGalleryFiles() {
        Log.d(TAG, "Botão de anexo de arquivos clicado. Abrindo a galeria de arquivos.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*"); // Define o tipo de arquivo (todos)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //================================================================< Abrir Galeria Audios >=================================================================================//

    // Método para abrir a galeria de áudios
    private void openGalleryAudio() {
        Log.d(TAG, "Botão de anexo de áudio clicado. Abrindo a galeria de áudios.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); // Define o tipo de arquivo como áudio
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //===================================================================< Convert BASE64 >=================================================================================//

    private String convertFileToBase64(Uri fileUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        byte[] fileBytes = IOUtils.toByteArray(inputStream);
        String base64File = Base64.encodeToString(fileBytes, Base64.DEFAULT);
        return base64File;
    }

    //========================================================================< OnStop >=================================================================================//

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Finalizando o app");
        //socketClient.close();
    }

    //====================================================================< Mostrar Mensagem >=================================================================================//

    public void showReceiveMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_other_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);
            }
        });
    }
}

//==============================================================================< Fim >=================================================================================//