/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pong.server;

import java.awt.Color;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import pong.cliente.Bola;

/**
 *
 * @author Craitson
 */
public class Server {

    private final double INICIO_TELA = -2.0;
    private final double FIM_TELA = 2.0;

    private ArrayList<NetPlayer> netPlayers = new ArrayList<NetPlayer>();
    private int count = 0;

    private Bola bola;

    public Server() {

        try {

            inicializarBola();

            ServerSocket servidor = new ServerSocket(5000);
            System.out.println("{Servidor} Porta de conexao 5000 aberta!");

            JanelaServer j = new JanelaServer();
            j.escreveConsole("{Servidor} Porta de conexao 5000 aberta!");

            while (true) {
                if (count < 2) {
                    Socket socket = servidor.accept(); // aceita conexao, apartir daqui o codigo continua se a conexao foi efetuada

                    NetPlayer aux = new NetPlayer(count, socket);

                    System.out.println("Clente conectado : ID " + aux.id);

                    count++;

                    netPlayers.add(aux);

                    new Thread(new Server.EscutaCliente(aux)).start();
                }
            }
        } catch (Exception ex) {
        }

    }

    private void inicializarBola() {
        bola = new Bola(0.480, 0.860, 0.023, 0.015, 0.05, Color.red);
    }

    private void encaminharPara(int id, String texto) {
        try {
            for (NetPlayer obj : netPlayers) {
                if (obj.id != id) {
                    obj.out.writeUTF(texto);
                    System.out.println("Encaminhado para " + obj.id + ", Conteúdo: " + texto);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Classe EscutaCliente. Cada cliente tera uma thread executando aqui. Essa
     * classe ouve a mensagem dos clientes, e envia apenas para o outro
     * cliente(adversario).
     */
    private class EscutaCliente implements Runnable {

        NetPlayer np;

        public EscutaCliente(NetPlayer np) {
            this.np = np;
        }

        @Override
        public void run() {
            String s = "->";
            try {
                while (true) {
                    s = np.in.readUTF(); // le a mensagem

                    if (Math.abs(bola.getPosicaoHorizontal() + bola.getVelocidadeHorizontal()) > FIM_TELA - bola.getRaio()) {
                        bola.inverteVelocidadeHorizontal();
                    }
                    if (Math.abs(bola.getPosicaoVertical() + bola.getVelocidadeVertical()) > FIM_TELA - bola.getRaio()) {
                        bola.inverteVelocidadeVertical();
                    }

                    bola.mover();

                    //Esse trecho inverte a posição da bola.
                    if (np.id == 0) {
                        encaminharPara(np.id, s + "," + bola.getPosicaoHorizontal() + "," + bola.getPosicaoVertical());
                    } else {
                        encaminharPara(np.id, s + "," + bola.getPosicaoHorizontal()*-1 + "," + bola.getPosicaoVertical()*-1);
                    }

                }
            } catch (Exception ex) {
            }
        }
    }

}
