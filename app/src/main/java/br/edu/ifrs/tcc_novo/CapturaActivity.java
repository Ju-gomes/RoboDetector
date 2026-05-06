package br.edu.ifrs.tcc_novo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.ifrs.tcc_novo.models.Robo;
import br.edu.ifrs.tcc_novo.repositories.RoboRepository;
import br.edu.ifrs.tcc_novo.service.OpenCvService;
import br.edu.ifrs.tcc_novo.utils.SessaoManager;

public class CapturaActivity extends AppCompatActivity {

    private static final String TAG = "CapturaActivity";

    private PreviewView previewView;
    private OpenCvService openCvService;
    private RoboRepository roboRepository;
    private SessaoManager sessaoManager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean permissaoCameraConcedida = false;
    private boolean isLogicaOpenCVIniciada = false;
    private Mat matPistaOriginal;
    private String caminhoPistaOriginal;
    private int usuarioId;

    private Button buttonIniciar, buttonPause, buttonStop, buttonSalvar;

    private ImageView imageViewOverlay;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    permissaoCameraConcedida = true;
                    iniciarCameraX();
                } else {
                    Toast.makeText(this, "Permissão da câmera é necessária.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV carregado no onResume.");

            if (openCvService == null) {
                openCvService = new OpenCvService();
            }

            // Inicializa o subtrator somente agora
            openCvService.initSubtractor();

//            javaCameraView.enableView();
        } else {
            Log.e(TAG, "Falha ao carregar OpenCV.");
            Toast.makeText(this, "Falha ao carregar OpenCV.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captura);

        previewView = findViewById(R.id.previewView);
        buttonIniciar = findViewById(R.id.buttonIniciar);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        imageViewOverlay = findViewById(R.id.imageViewOverlay);

        sessaoManager = new SessaoManager(this);
        usuarioId = sessaoManager.getUserIdLogado();
        if (usuarioId == -1) {
            Toast.makeText(this, "Erro de sessão", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        caminhoPistaOriginal = getIntent().getStringExtra("PISTA_CAMINHO_IMAGEM");
        if (caminhoPistaOriginal == null || caminhoPistaOriginal.isEmpty()) {
            Toast.makeText(this, "Erro: caminho da imagem da pista não encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        roboRepository = new RoboRepository(getApplication(), usuarioId);
        openCvService = new OpenCvService();

//        buttonIniciar.setOnClickListener(v -> openCvService.iniciarProcessamento());
//        buttonPause.setOnClickListener(v -> openCvService.pausarProcessamento());
//        buttonStop.setOnClickListener(v -> openCvService.pararProcessamento());
//        buttonSalvar.setOnClickListener(v -> mostrarDialogSalvar());

        buttonIniciar.setOnClickListener(v -> {
            openCvService.iniciarProcessamento();
            Toast.makeText(this, "Captura iniciada!", Toast.LENGTH_SHORT).show();
            atualizarEstadoBotoes();
        });

        buttonPause.setOnClickListener(v -> {
            openCvService.pausarProcessamento();
            Toast.makeText(this, "Captura pausada.", Toast.LENGTH_SHORT).show();
            atualizarEstadoBotoes();
        });

        buttonStop.setOnClickListener(v -> {
            openCvService.pararProcessamento();
            Toast.makeText(this, "Captura parada.", Toast.LENGTH_SHORT).show();
            atualizarEstadoBotoes();
        });

        buttonSalvar.setOnClickListener(v -> {
            if (openCvService.isProcessando()) {
                openCvService.pausarProcessamento();
            }
            mostrarDialogSalvar();
        });

        atualizarEstadoBotoes();
        verificarPermissaoCamera();
    }

    private void atualizarEstadoBotoes() {
        boolean pistaPronta = isLogicaOpenCVIniciada;
        boolean processando = openCvService.isProcessando();

        buttonIniciar.setEnabled(pistaPronta && !processando);
        buttonPause.setEnabled(pistaPronta && processando);
        buttonStop.setEnabled(pistaPronta && processando);
        buttonSalvar.setEnabled(pistaPronta);

        // Feedback visual
        buttonIniciar.setAlpha(pistaPronta && !processando ? 1.0f : 0.5f);
        buttonPause.setAlpha(pistaPronta && processando ? 1.0f : 0.5f);
        buttonStop.setAlpha(pistaPronta && processando ? 1.0f : 0.5f);
        buttonSalvar.setAlpha(pistaPronta ? 1.0f : 0.5f);
    }

    private void verificarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permissaoCameraConcedida = true;
            iniciarCameraX();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCameraX() {
        if (!permissaoCameraConcedida) return;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, this::analisarFrame);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);

                carregarImagemDaPista(caminhoPistaOriginal);

            } catch (Exception e) {
                Log.e(TAG, "Erro ao iniciar CameraX", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private long ultimoFrame = 0;
    private static final long FRAME_INTERVAL = 50;

//    private void analisarFrame(ImageProxy imageProxy) {
//        if (!isLogicaOpenCVIniciada) {
//            imageProxy.close();
//            return;
//        }
//
//        long agora = System.currentTimeMillis();
//        if (agora - ultimoFrame < FRAME_INTERVAL) {
//            imageProxy.close();
//            return;
//        }
//        ultimoFrame = agora;
//
//        try {
//            Mat matFrame = openCvService.imageProxyParaMat(imageProxy);
//            Mat processado = openCvService.processarFrame(matFrame);
//
//            Bitmap bitmapProcessado = openCvService.matParaBitmap(processado);
//
//            mainHandler.post(() -> {
//                Drawable old = imageViewOverlay.getDrawable();
//                if (old instanceof BitmapDrawable) {
//                    Bitmap oldBmp = ((BitmapDrawable) old).getBitmap();
//                    if (oldBmp != null && !oldBmp.isRecycled()) {
//                        oldBmp.recycle();
//                    }
//                }
//                imageViewOverlay.setImageBitmap(bitmapProcessado);
//            });
//
//            matFrame.release();
//            processado.release();
//        } catch (Exception e) {
//            Log.e(TAG, "Erro ao processar frame", e);
//        } finally {
//            imageProxy.close();
//        }
//    }

    private void analisarFrame(ImageProxy imageProxy) {
        if (!isLogicaOpenCVIniciada) {
            imageProxy.close();
            return;
        }

        long agora = System.currentTimeMillis();
        if (agora - ultimoFrame < FRAME_INTERVAL) {
            imageProxy.close();
            return;
        }
        ultimoFrame = agora;

        try {
            Mat matFrame = openCvService.imageProxyParaMat(imageProxy);

            // ### CORREÇÃO AQUI ###
            // Se for o primeiro frame (ou se o tamanho ainda não foi setado),
            // informa o serviço qual é o tamanho do frame da câmera.
            if (openCvService.getLarguraFrame() == 0) {
                // Usamos cols() para largura e rows() para altura
                openCvService.setTamanhoFrame(matFrame.cols(), matFrame.rows());
            }
            // ### FIM DA CORREÇÃO ###


            Mat processado = openCvService.processarFrame(matFrame);

            Bitmap bitmapProcessado = openCvService.matParaBitmap(processado);

            mainHandler.post(() -> {
                Drawable old = imageViewOverlay.getDrawable();
                if (old instanceof BitmapDrawable) {
                    Bitmap oldBmp = ((BitmapDrawable) old).getBitmap();
                    if (oldBmp != null && !oldBmp.isRecycled()) {
                        oldBmp.recycle();
                    }
                }
                imageViewOverlay.setImageBitmap(bitmapProcessado);
            });

            matFrame.release();
            processado.release();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar frame", e);
        } finally {
            imageProxy.close();
        }
    }

//    private void carregarImagemDaPista(String caminho) {
//        executor.execute(() -> {
//            try {
//                Bitmap bitmapPista = BitmapFactory.decodeFile(new File(caminho).getAbsolutePath());
//                if (bitmapPista == null) throw new IOException("Falha ao decodificar bitmap da pista");
//
//                matPistaOriginal = openCvService.bitmapParaMat(bitmapPista);
//                openCvService.prepararImagemPista(matPistaOriginal);
//
//                mainHandler.post(() -> isLogicaOpenCVIniciada = true);
//
//                atualizarEstadoBotoes();
//            } catch (Exception e) {
//                Log.e(TAG, "Erro ao carregar pista", e);
//                mainHandler.post(() -> Toast.makeText(this, "Falha ao carregar a pista.", Toast.LENGTH_LONG).show());
//            }
//        });
//    }

    private void carregarImagemDaPista(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            mostrarErroECair("Caminho da pista inválido.");
            return;
        }

        File file = new File(caminho);
        if (!file.exists()) {
            mostrarErroECair("Arquivo da pista não encontrado: " + caminho);
            return;
        }

        if (!isImagemValida(file)) {
            mostrarErroECair("Arquivo não é uma imagem válida.");
            return;
        }

        // Mostrar feedback
        mainHandler.post(() -> Toast.makeText(this, "Carregando pista...", Toast.LENGTH_SHORT).show());

        executor.execute(() -> {
            try {
                Bitmap bitmapPista = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmapPista == null) {
                    throw new IOException("Falha ao decodificar imagem (bitmap nulo)");
                }

                Mat matPista = openCvService.bitmapParaMat(bitmapPista);
                if (matPista.empty()) {
                    throw new IOException("Mat OpenCV vazio após conversão");
                }

                openCvService.prepararImagemPista(matPista);

                // Sucesso!
                mainHandler.post(() -> {
                    isLogicaOpenCVIniciada = true;
                    Toast.makeText(this, "Pista carregada com sucesso!", Toast.LENGTH_SHORT).show();
                    atualizarEstadoBotoes(); // Habilita botões
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar pista: " + e.getMessage(), e);
                mainHandler.post(() -> mostrarErroECair("Erro ao carregar pista: " + e.getMessage()));
            }
        });
    }

    private boolean isImagemValida(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp");
    }

    private void mostrarErroECair(String mensagem) {
        runOnUiThread(() -> {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
        });
    }

    private void mostrarDialogSalvar() {
        openCvService.pausarProcessamento();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salvar Trajeto do Robô");
        EditText inputNome = new EditText(this);
        inputNome.setHint("Nome do Robô");
        builder.setView(inputNome);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nomeRobo = inputNome.getText().toString().trim();
            if (!TextUtils.isEmpty(nomeRobo)) salvarTrajetoRobo(nomeRobo);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void salvarTrajetoRobo(String nomeRobo) {
        // 1. GERA A IMAGEM DE COMPARAÇÃO (pista vermelha + trajeto verde)
        Bitmap comparacaoBitmap = openCvService.getTrajetoComparacaoBitmap();

        if (comparacaoBitmap == null || comparacaoBitmap.getWidth() == 0) {
            Toast.makeText(this, "Erro ao gerar imagem do trajeto", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. SALVA A IMAGEM DE COMPARAÇÃO (essa é a que vai pro banco!)
        String caminhoSalvo = salvarBitmapEmArquivo(comparacaoBitmap, nomeRobo);
        Log.d("SALVAR", "Trajeto + pista salvo em: " + caminhoSalvo);

        if (caminhoSalvo == null) {
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Salva no banco (agora o caminho aponta para a imagem com pista + trajeto)
        Robo robo = new Robo();
        robo.setUsuarioId(usuarioId);
        robo.setNome(nomeRobo);
        robo.setCaminhoImagemTrajeto(caminhoSalvo);  // ← Aqui está a imagem bonita de comparação!

        roboRepository.insert(robo);

        // Opcional: reciclar o bitmap grande pra liberar memória
        comparacaoBitmap.recycle();

        Toast.makeText(this, "Robô salvo com sucesso!", Toast.LENGTH_SHORT).show();

        // Limpa e volta
        openCvService.pararProcessamento();
        finish();
    }

    private String salvarBitmapEmArquivo(Bitmap bitmap, String nomeRobo) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "TRAJETO_" + nomeRobo.replaceAll("\\s+", "_") + "_" + timeStamp + ".jpg";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar bitmap", e);
            return null;
        }
    }
}
