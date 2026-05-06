// ExibirTrajetoActivity.java
package br.edu.ifrs.tcc_novo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ExibirTrajetoActivity extends AppCompatActivity {

    private Button buttonVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exibir_trajeto);

        ImageView imageView = findViewById(R.id.imageViewTrajeto);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        buttonVoltar.setOnClickListener(v -> finish());

        String caminho = getIntent().getStringExtra("CAMINHO_TRAJETO");

        Log.d("EXIBIR", "Caminho recebido: " + caminho);

        if (caminho == null || caminho.isEmpty()) {
            Log.e("EXIBIR", "Caminho nulo ou vazio!");
            imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            return;
        }

        File file = new File(caminho);
        Log.d("EXIBIR", "Arquivo existe? " + file.exists());
        Log.d("EXIBIR", "Caminho absoluto: " + file.getAbsolutePath());

        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                Log.d("EXIBIR", "Bitmap carregado: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
            } else {
                Log.e("EXIBIR", "BitmapFactory retornou NULL!");
                imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            }
        } else {
            Log.e("EXIBIR", "Arquivo NÃO existe!");
            imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }
}