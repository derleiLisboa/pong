package pong.cliente;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Derlei
 */
public class Jogo implements Runnable {

    private final String PATH_PROJETO = "C:\\Users\\Derlei\\Documents\\NetBeansProjects\\Pong 2.0\\src\\";
    private final String PATH_IMAGEM_PLANO_DE_FUNDO = PATH_PROJETO + "img\\aa.jpg";

    private final double INICIO_TELA = -2.0;
    private final double FIM_TELA = 2.0;

    private String ip;
    
    private Jogador jogador;
    private Barra barra1;
    private Barra barra2;
    private Bola bola;

    private BolaEstatica vidaUm;
    private BolaEstatica vidaDois;
    private BolaEstatica vidaTres;

    private int velocidade = 14;
    private int contadorPing = 1;
    private AudioClip audio;
    private Socket socket;

    private DataOutputStream out;
    private DataInputStream in;

    @Override
    public void run() {
        inicializarJogo();

        StdDraw.setXscale(INICIO_TELA, FIM_TELA);
        StdDraw.setYscale(INICIO_TELA, FIM_TELA);

        try {
            socket = new Socket(ip, 5000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            new Thread(new EscutaServidor()).start();
        } catch (IOException ex) {}

        while (true) {
            try {
                // send to server
                out.writeUTF(Double.toString(barra1.getPosicaoHorizontal()));
            } catch (IOException ex) {}

            imprimirPlanoDeFundo();
            imprimirBola();
            imprimirBarra();

            verificaTeclasPressionadas();
            StdDraw.show(velocidade);
        }
    }

    private void inicializarJogo() {
        inicializarBola();
        inicializarBarra1();
    }

    private void inicializarBola() {
        bola = new Bola(0.480, 0.860, 0.023, 0.015, 0.05, Color.red);
    }

    private void reinicializarBola() {
        bola = new Bola(0.480, 0.860, bola.getVelocidadeVertical(), 0.023, 0.05, Color.red);
    }

    private void inicializarBarra1() {
        barra1 = new Barra(-1.95, 0.01, 0.015, 0, 0.30, 0.05, Color.gray);
        barra2 = new Barra(1.95, 0.01, 0.015, 0, 0.30, 0.05, Color.gray);
    }

    private void imprimirPlanoDeFundo() {
        StdDraw.clear(Color.white);
        StdDraw.picture(1, 0, PATH_IMAGEM_PLANO_DE_FUNDO);
    }

    private void imprimirBola() {
        StdDraw.setPenColor(bola.getCor());
        StdDraw.filledCircle(bola.getPosicaoHorizontal(), bola.getPosicaoVertical(), bola.getRaio());
    }

    private void imprimirBarra() {
        StdDraw.setPenColor(barra1.getCor());
        StdDraw.filledRectangle(barra1.getPosicaoHorizontal(), barra1.getPosicaoVertical(), barra1.getComprimento(), barra1.getEspessura());
        StdDraw.filledRectangle(barra2.getPosicaoHorizontal(), barra2.getPosicaoVertical(), barra2.getComprimento(), barra2.getEspessura());
    }

    private void verificaTeclasPressionadas() {
        if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) {
            if (!(barra1.getPosicaoHorizontal() <= INICIO_TELA + barra1.getComprimento())) {
                barra1.moveEsquerda();
            }
        }

        //captura tecla direita
        if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) {
            if (!(barra1.getPosicaoHorizontal() >= FIM_TELA - barra1.getComprimento())) {
                barra1.moveDireita();
            }
        }
    }

    private class EscutaServidor implements Runnable {

        /*
         * Sobrescreve metodo run
         */
        @Override
        public void run() {

            String s = "";

            try {
                while (true) {

                    s = in.readUTF();
                    
                    String[] split = s.split(",");
                    
                    barra2.setPosicaoHorizontal(Double.parseDouble(split[0])* -1);
                    
                    bola.setPosicaoHorizontal(Double.parseDouble(split[1]));
                    bola.setPosicaoVertical(Double.parseDouble(split[2]));
                    
                    
                    System.out.println(s);
                }
            } catch (Exception ex) {
                System.out.println("Erro: " + ex.getMessage());
            }
        }
    }

    
    public void setIp(String ip) {
        this.ip = ip;
    }
}
