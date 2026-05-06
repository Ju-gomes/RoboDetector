package br.edu.ifrs.tcc_novo.service;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de serviço para encapsular toda a lógica de processamento de imagem
 * com OpenCV. (Baseado no arquivo fornecido)
 *
 * NOTA: O método detectarLinhaDeChegada() será ignorado conforme solicitado.
 */
public class OpenCvService {

    private static final String TAG = "OpenCvService";

    private BackgroundSubtractorMOG2 subtrator;
    private final List<Point> pontosTrajeto;
    private Mat matTrajeto;
    private List<MatOfPoint> contornosPista;

    // Flag para controlar o processamento
    private boolean processando = false;

    private int larguraFrame = 0, alturaFrame = 0;
    private Mat matPistaOriginal;

    private final Scalar COR_TRAJETO = new Scalar(0, 255, 0); // Verde
    private final Scalar COR_PISTA = new Scalar(255, 0, 0);   // Vermelho
    private final Scalar COR_ROBO = new Scalar(0, 0, 255);    // Azul

    private final double MIN_AREA_CONTORNO_ROBO = 100.0;

    // No topo da classe (campos)
    private List<MatOfPoint> contornosPistaRedimensionados = new ArrayList<>();

    // (Código comentado da versão anterior removido para limpeza)

    /**
     * NOVO MÉTODO CORRIGIDO:
     * Redimensiona os contornos da pista original para caberem no frame da câmera.
     */
    public void redimensionarContornosPista() {
        if (contornosPista == null || contornosPista.isEmpty() || larguraFrame == 0 || matPistaOriginal == null || matPistaOriginal.empty()) {
            Log.w(TAG, "Redimensionamento ignorado (contornos, frame ou pista original não prontos)");
            return;
        }
        contornosPistaRedimensionados.clear();

        double escalaX = (double) larguraFrame / matPistaOriginal.width();
        double escalaY = (double) alturaFrame / matPistaOriginal.height();
        // Usa 'min' para manter a proporção e garantir que caiba na tela
        double escala = Math.min(escalaX, escalaY);

        Log.i(TAG, "Redimensionando contornos. Frame=" + larguraFrame + "x" + alturaFrame +
                ", Pista=" + matPistaOriginal.width() + "x" + matPistaOriginal.height() +
                ", Escala=" + escala);

        for (MatOfPoint src : contornosPista) {
            MatOfPoint contorno = new MatOfPoint();
            Point[] p = src.toArray();
            Point[] q = new Point[p.length];
            for (int i = 0; i < p.length; i++) {
                // Aplica a escala calculada
                q[i] = new Point(p[i].x * escala, p[i].y * escala);
            }
            contorno.fromArray(q);
            contornosPistaRedimensionados.add(contorno);
        }
    }


    /**
     * Desenha a pista usando os contornos já redimensionados.
     */
    private void desenharPistaRedimensionada(Mat frame) {
        if (!contornosPistaRedimensionados.isEmpty()) {
            Imgproc.drawContours(frame, contornosPistaRedimensionados, -1, COR_PISTA, 1);
        }
    }

    public List<MatOfPoint> getContornosPistaRedimensionados() {
        return contornosPistaRedimensionados;
    }

    public OpenCvService() {
        this.pontosTrajeto = new ArrayList<>();
        this.contornosPista = new ArrayList<>();
        this.processando = false; // Inicia pausado
        // NÃO criar o subtrator ainda
    }

    // Chame este método só após OpenCV estar carregado
    public void initSubtractor() {
        if (subtrator == null) {
            subtrator = Video.createBackgroundSubtractorMOG2(500, 30, false);
        }
    }


    /**
     * Inicia ou retoma o processamento de frames.
     */
    public void iniciarProcessamento() {
        this.processando = true;
    }

    /**
     * Pausa o processamento de frames.
     */
    public void pausarProcessamento() {
        this.processando = false;
    }

    /**
     * Para e limpa o trajeto atual.
     */
    public void pararProcessamento() {
        this.processando = false;
        this.pontosTrajeto.clear();
        if (this.matTrajeto != null) {
            this.matTrajeto.release();
            this.matTrajeto = null;
        }
    }

    public boolean isProcessando() {
        return this.processando;
    }

    /**
     * Método principal chamado a cada frame pela CapturaActivity.
     */
    public Mat processarFrame(Mat frameAtual) {
        // Se não estiver processando, apenas desenha a pista e o trajeto existente
        if (!processando) {
            // Desenha a pista redimensionada (se já existir)
            desenharPistaRedimensionada(frameAtual);
            // Desenha o trajeto capturado até agora
            desenharTrajeto(frameAtual);
            return frameAtual;
        }

        // Garante que o matTrajeto tenha o tamanho do frame da câmera
        if (matTrajeto == null || !matTrajeto.size().equals(frameAtual.size())) {
            if (matTrajeto != null) {
                matTrajeto.release();
            }
            // Cria um Mat transparente (alpha 0)
            matTrajeto = new Mat(frameAtual.size(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
        }

        // 1. Identifica o robô (objeto em movimento)
        Point centroRobo = identificarObjeto(frameAtual);

        if (centroRobo != null) {
            pontosTrajeto.add(centroRobo);
            Imgproc.circle(frameAtual, centroRobo, 20, COR_ROBO, 3);
        }

        // 2. Desenha o trajeto (sempre)
        desenharTrajeto(frameAtual);

        // 3. Desenha os contornos da pista REDIMENSIONADA sobre o frame
        desenharPistaRedimensionada(frameAtual);

        return frameAtual;
    }

    /**
     * Encontra os contornos da imagem da pista (fundo branco, linha preta).
     * MÉTODO ATUALIZADO
     */
    public void prepararImagemPista(Mat matPista) {
        // Armazena a pista original para referência de tamanho no redimensionamento
        this.matPistaOriginal = matPista.clone();

        Mat cinza = new Mat();
        Imgproc.cvtColor(matPista, cinza, Imgproc.COLOR_RGBA2GRAY);
        Mat binario = new Mat();
        // Inverte (THRESH_BINARY_INV) para que a linha preta vire branca (objeto)
        Imgproc.threshold(cinza, binario, 70, 255, Imgproc.THRESH_BINARY_INV);

        // --------------------------------------------------------------
        // 2. Remove ruídos pequenos e fecha buracos na linha (opcional mas recomendado)
        // --------------------------------------------------------------
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.morphologyEx(binario, binario, Imgproc.MORPH_CLOSE, kernel, new Point(-1,-1), 2);
        Imgproc.morphologyEx(binario, binario, Imgproc.MORPH_OPEN, kernel, new Point(-1,-1), 1);

        contornosPista.clear();
        Mat hierarquia = new Mat();
        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(binario, contornosPista, hierarquia, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double areaMinima = 500;
        for(MatOfPoint m : contornos){
            if(Imgproc.contourArea(m) > areaMinima){
                contornosPista.add(m);
            }
        }
        Log.d(TAG, "Contornos da pista encontrados: " + contornosPista.size());

        // ### CORREÇÃO AQUI ###
        // Se o tamanho do frame já é conhecido (foi definido antes),
        // manda redimensionar os contornos agora
        if (this.larguraFrame > 0) {
            redimensionarContornosPista();
        }
        // ### FIM DA CORREÇÃO ###

        cinza.release();
        binario.release();
        hierarquia.release();
    }

    /**
     * Identifica um objeto em movimento (robô) usando subtração de fundo.
     */
    private Point identificarObjeto(Mat frame) {
        Mat mascaraFg = new Mat();

        // ### MUDANÇA 1: Taxa de Aprendizagem ###
        // Reduzimos de 0.01 para 0.005.
        // Isso torna o subtrator mais sensível a movimentos menores/mais lentos,
        // pois o "fundo" demora mais para se adaptar.
        subtrator.apply(frame, mascaraFg, 0.005); // <-- MUDANÇA

        // ### MUDANÇA 2: Operações Morfológicas ###
        // Reduzimos de 10 iterações (muito alto) para valores pequenos.
        // Isso remove ruído pequeno sem apagar o robô.
        Imgproc.erode(mascaraFg, mascaraFg, new Mat(), new Point(-1, -1), 2); // <-- MUDANÇA (de 10 para 2)
        Imgproc.dilate(mascaraFg, mascaraFg, new Mat(), new Point(-1, -1), 3); // <-- MUDANÇA (de 10 para 3)
        // (Usamos 3 na dilatação para "reforçar" um pouco o objeto após a erosão)

        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(mascaraFg, contornos, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        Point centroRobo = null;
        for (MatOfPoint contorno : contornos) {
            double area = Imgproc.contourArea(contorno);

            // ### MUDANÇA 3: Área Mínima ###
            // Agora usa a constante (150.0) que ajustamos.
            if (area > MIN_AREA_CONTORNO_ROBO) { // <-- MUDANÇA (agora 150.0)
                if (area > maxArea) {
                    maxArea = area;
                    org.opencv.core.Rect boundRect = Imgproc.boundingRect(contorno);
                    centroRobo = new Point(
                            boundRect.x + boundRect.width / 2.0,
                            boundRect.y + boundRect.height / 2.0
                    );
                }
            }
        }
        mascaraFg.release();
        return centroRobo;
    }

    /**
     * Desenha o trajeto percorrido no frame da câmera.
     */
    private void desenharTrajeto(Mat frame) {
        if (pontosTrajeto.size() < 2) {
            return;
        }

        for (int i = 1; i < pontosTrajeto.size(); i++) {
            Point p1 = pontosTrajeto.get(i - 1);
            Point p2 = pontosTrajeto.get(i);
            // Desenha no frame da câmera
            Imgproc.line(frame, p1, p2, COR_TRAJETO, 4);

            // Garante que matTrajeto exista ao desenhar (para salvar depois)
            if (matTrajeto != null) {
                Imgproc.line(matTrajeto, p1, p2, COR_TRAJETO, 4);
            }
        }
    }

    /**
     * Gera imagem final com o trajeto do robô (verde) sobreposto à pista (contorno vermelho)
     * Ideal para salvar ou mostrar a comparação perfeita
     */
    public Bitmap getTrajetoComparacaoBitmap() {
        if (larguraFrame == 0 || alturaFrame == 0) {
            Log.w(TAG, "Tamanho do frame não definido ainda");
            return Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        }

        // 1. Cria um canvas completamente branco do tamanho do frame da câmera
        Mat resultado = new Mat(alturaFrame, larguraFrame, CvType.CV_8UC4, new Scalar(0, 0, 0, 0));

        // 2. Desenha o contorno da pista em VERMELHO (exatamente como já aparece na tela ao vivo)
        if (!contornosPistaRedimensionados.isEmpty()) {
            Imgproc.drawContours(resultado, contornosPistaRedimensionados, -1, COR_PISTA, 5); // vermelho, grossura 5 fica bem visível
        }

        // 3. Desenha o trajeto do robô em VERDE por cima
        if (matTrajeto != null && !matTrajeto.empty()) {
            // matTrajeto tem canal alpha com o trajeto em verde
            Core.addWeighted(resultado, 1.0, matTrajeto, 1.0, 0.0, resultado);
        }

        // 4. Converte para Bitmap
        Bitmap bitmapFinal = matParaBitmap(resultado);

        // 5. Libera memória
        resultado.release();

        return bitmapFinal;
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Cria um MatOfPoint (contorno) a partir da lista de pontos do trajeto.
     * O contorno é suavizado para remover ruídos e "fechado"
     * (não é um contorno fechado, mas sim um polígono que representa o trajeto).
     */
    private MatOfPoint criarContornoDoTrajeto() {
        if (pontosTrajeto.size() < 2) {
            return null;
        }

        // 1. Cria o MatOfPoint a partir da lista de pontos
        MatOfPoint contorno = new MatOfPoint();
        contorno.fromList(pontosTrajeto);

        // 2. Simplificação/Suavização do contorno (importante para MatchShapes)
        MatOfPoint2f contorno2f = new MatOfPoint2f();
        contorno.convertTo(contorno2f, CvType.CV_32F);

        MatOfPoint2f approxContorno2f = new MatOfPoint2f();
        // Usa Douglas-Peucker para aproximar o polígono (suavizar o trajeto)
        // O 2.0 é um parâmetro de epsilon que define o quão agressiva é a simplificação
        Imgproc.approxPolyDP(contorno2f, approxContorno2f, 2.0, false);

        MatOfPoint approxContorno = new MatOfPoint();
        approxContorno2f.convertTo(approxContorno, CvType.CV_32S);

        // Liberação de memória
        contorno.release();
        contorno2f.release();
        approxContorno2f.release();

        return approxContorno;
    }

    public Mat bitmapParaMat(Bitmap bmp) {
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        return mat;
    }

    public Bitmap matParaBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }

    public Bitmap getTrajetoFinalBitmap() {
        if (matTrajeto == null) {
            Log.w(TAG, "matTrajeto está nulo, retornando bitmap vazio.");
            // Tenta usar o tamanho do frame se conhecido, senão usa um padrão
            int w = (larguraFrame > 0) ? larguraFrame : 640;
            int h = (alturaFrame > 0) ? alturaFrame : 480;
            return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        return matParaBitmap(matTrajeto);
    }

    public Mat imageProxyParaMat(ImageProxy imageProxy) {
        // Pega o primeiro plano (Y - Luminância, ou seja, tons de cinza)
        ImageProxy.PlaneProxy plane = imageProxy.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        // Cria um Mat (CvType.CV_8UC1) com os dados do plano Y (grayscale)
        Mat mat = new Mat(imageProxy.getHeight(), imageProxy.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, bytes);

        // Converte o Mat de tons de cinza (GRAY) para RGBA
        // O subtrator de fundo e o desenho funcionarão bem com isso.
        Mat matRGBA = new Mat();
        Imgproc.cvtColor(mat, matRGBA, Imgproc.COLOR_GRAY2RGBA, 4);

        // NOTA: Se a imagem da câmera aparecer DE LADO (90 graus),
        // você pode precisar rotacionar ela aqui.
        // Se isso acontecer, descomente a linha abaixo:
        // Core.rotate(matRGBA, matRGBA, Core.ROTATE_90_CLOCKWISE);

        mat.release();
        return matRGBA;
    }

    /**
     * MÉTODO ATUALIZADO
     * Define o tamanho do frame (vindo da câmera) e recalcula os contornos.
     */
    public void setTamanhoFrame(int w, int h) {
        // Evita reprocessar se o tamanho não mudou
        if (this.larguraFrame == w && this.alturaFrame == h) {
            return;
        }

        this.larguraFrame = w;
        this.alturaFrame = h;
        Log.d(TAG, "Tamanho do frame definido: " + w + "x" + h);

        // ### CORREÇÃO AQUI ###
        // Se a pista já foi carregada (contornos existem),
        // manda redimensionar os contornos agora
        if (contornosPista != null && !contornosPista.isEmpty()) {
            redimensionarContornosPista();
        }
        // ### FIM DA CORREÇÃO ###
    }

    public int getLarguraFrame() {
        return larguraFrame;
    }

}