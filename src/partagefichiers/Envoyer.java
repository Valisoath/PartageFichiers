/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Random;

public class Envoyer {

    public static void main(String[] args) {

        try {

            // Obtenir l'adresse IP locale de l'envoyeur
            InetAddress localAddress = InetAddress.getLocalHost();
            String senderIP = localAddress.getHostAddress();

            // Obtenir le nom de la machine de l'envoyeur
            String senderHostName = InetAddress.getByName(senderIP).getHostName();

            final int PORT = 9999;

            File selectedFile = chooseFile();
            try (Socket socket = new Socket(senderIP, PORT); FileInputStream fileInputStream = new FileInputStream(selectedFile)) {

                // Générer un code aléatoire de 5 chiffres
                int randomCode = generateRandomCode();

                // Afficher le code dans une boîte de dialogue
                JOptionPane.showMessageDialog(null, "Votre code de transfert est : " + randomCode, "Code de transfert", JOptionPane.INFORMATION_MESSAGE);

                // Envoyer le code au récepteur
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(randomCode);

                // Envoyer le nom de la machine de l'envoyeur avant le fichier
                dos.writeUTF(senderHostName);

                // Obtenir le nom du fichier sans extension
                String fileName = selectedFile.getName();
                String extension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    extension = fileName.substring(dotIndex);
                    fileName = fileName.substring(0, dotIndex);
                }

                // Envoyer le nom du fichier et son extension
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static int generateRandomCode() {
        Random random = new Random();
        return 10000 + random.nextInt(90000); // Génère un nombre aléatoire entre 10000 et 99999 inclus
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