/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class Envoyer {

    public static void main(String[] args) {

        try {
            // Obtenir le nom de la machine de l'envoyeur
            String senderName = InetAddress.getLocalHost().getHostName();

            // Afficher une boîte de dialogue pour l'approbation du récepteur
            int confirmation = JOptionPane.showConfirmDialog(null, "Voulez-vous envoyer le fichier à " + senderName + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                // Obtenir l'adresse IP locale de l'envoyeur
                InetAddress localAddress = InetAddress.getLocalHost();
                String senderIP = localAddress.getHostAddress();

                final int PORT = 9999;

                File selectedFile = chooseFile();
                try (Socket socket = new Socket(senderIP, PORT); FileInputStream fileInputStream = new FileInputStream(selectedFile)) {

                    // Obtenir le nom du fichier sans extension
                    String fileName = selectedFile.getName();
                    String extension = "";
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex != -1) {
                        extension = fileName.substring(dotIndex);
                        fileName = fileName.substring(0, dotIndex);
                    }

                    // Envoyer le nom du fichier et son extension
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(fileName);
                    dos.writeUTF(extension);

                    // Envoyer le contenu du fichier au serveur
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    OutputStream outputStream = socket.getOutputStream();
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Fichier envoyé avec succès !");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Envoi annulé par le récepteur.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            throw new RuntimeException("Aucun fichier sélectionné !");
        }
    }
}