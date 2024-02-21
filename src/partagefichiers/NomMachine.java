/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import java.io.*;
import java.net.*;

/**
 *
 * @author melobk7
 */
public class NomMachine {
    private static final int PORT = 9999;
    private static final String BROADCAST_ADDRESS = "255.255.255.255"; // Adresse de diffusion

    public boolean envoyeNomMachine() {
        boolean isEnvoyer = false;
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress localAddress = InetAddress.getLocalHost();
            String senderName = localAddress.getHostName();

            while (true) {
                String message = "SENDER:" + senderName;
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(BROADCAST_ADDRESS), PORT);
                socket.send(packet);

                Thread.sleep(5000); // Envoyer le message de diffusion toutes les 5 secondes
                isEnvoyer = true;
            }
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return isEnvoyer;
    }
}
