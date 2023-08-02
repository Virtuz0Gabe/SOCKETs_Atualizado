package com.example.desafio_6_sockets.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.desafio_6_sockets.Controller.ChatClient;
import com.example.desafio_6_sockets.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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

                RelativeLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_own_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);

                ScrollView scrollView = findViewById(R.id.scrollViewMessages);
                scrollView.fullScroll(View.FOCUS_DOWN);
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
                                // mostrar a imagem para quem está enviando
                                RelativeLayout layoutParent = findViewById(R.id.message_space);
                                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_img_own, null);
                                ImageView imageView = layoutChild.findViewById(R.id.img_inflate_own);
                                imageView.setImageURI(selectedFileUri);
                                layoutParent.addView(layoutChild);

                                try {
                                    String imageBase64 = convertFileToBase64(selectedFileUri);
                                    Log.i(TAG, String.valueOf(imageBase64));
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
                // Inflar o layout do Modal
                View modalLayout = getLayoutInflater().inflate(R.layout.alert_dialog_custom, null);

                // Criar o Modal
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                bottomSheetDialog.setContentView(modalLayout);

                // Obter o FrameLayout do activity_main.xml

                // Remover o modalLayout de seu pai atual, se ele já tiver um pai
                if (modalLayout.getParent() != null) {
                    ((ViewGroup) modalLayout.getParent()).removeView(modalLayout);
                }

                //frameLayout.addView(modalLayout); // Adicionar o layout do modal ao FrameLayout

                bottomSheetDialog.show();

                // Obter o botão de fechar do layout do modal
                ImageView buttonFechar = modalLayout.findViewById(R.id.button_fechar);
                buttonFechar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss(); // Fechar o modal
                    }
                });

                // Configurar os botões no layout do Modal
                LinearLayout buttonGaleria = modalLayout.findViewById(R.id.button_galeria);
                buttonGaleria.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss(); // Fechar o Modal antes de realizar a ação
                        openGalleryIMG();
                    }
                });

                LinearLayout buttonEnviarArquivo = modalLayout.findViewById(R.id.button_enviar_arquivo);
                buttonEnviarArquivo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss(); // Fechar o Modal antes de realizar a ação
                        openGalleryFiles();
                    }
                });

                LinearLayout buttonEnviarAudio = modalLayout.findViewById(R.id.button_enviar_audio);
                buttonEnviarAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss(); // Fechar o Modal antes de realizar a ação
                        openGalleryAudio();
                    }
                });
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
                RelativeLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_other_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);

                ScrollView scrollView = findViewById(R.id.scrollViewMessages);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void showReceiveImage(Uri UriImagem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_img_own, null);
                ImageView imageView = layoutChild.findViewById(R.id.img_inflate_other);
                imageView.setImageURI(UriImagem);
                layoutParent.addView(layoutChild);
            }
        });
    }
}

//==============================================================================< Fim >=================================================================================//