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

    public void envoieFichier(JTabbedPane jTabbedPane1) {

        final int PORT = 9999;

        // Afficher un message indiquant que l'envoyeur est en attente de récepteur
        System.out.println("En attente de récepteur...");

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Attendre une connexion pendant 20 secondes
            serverSocket.setSoTimeout(20000); // 20 secondes
            Socket clientSocket = serverSocket.accept();

            // Si une connexion est établie, continuer le processus d'envoi
            System.out.println("Récepteur connecté !");
            InetAddress localAddress = InetAddress.getLocalHost();
            String senderIP = localAddress.getHostAddress();
            String senderHostName = InetAddress.getByName(senderIP).getHostName();

            File selectedFile = chooseFile();
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                // Générer un code aléatoire de 5 chiffres
                int randomCode = generateRandomCode();

                // Afficher le code dans une boîte de dialogue
                JOptionPane.showMessageDialog(null, "Votre code de transfert est : " + randomCode, "Code de transfert", JOptionPane.INFORMATION_MESSAGE);

                // Envoyer le code au récepteur
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
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

                // Envoyer le contenu du fichier au récepteur
                byte[] buffer = new byte[8192];
                int bytesRead;
                OutputStream outputStream = clientSocket.getOutputStream();
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Fichier envoyé avec succès !");
                jTabbedPane1.setSelectedIndex(5);
            }
        } catch (SocketTimeoutException e) {
            // Afficher un message d'erreur si aucun récepteur n'est connecté après 20 secondes
            JOptionPane.showMessageDialog(null, "Aucun récepteur n'a répondu dans les 20 secondes.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int generateRandomCode() {
        Random random = new Random();
        return 10000 + random.nextInt(90000); // Génère un nombre aléatoire entre 10000 et 99999 inclus
    }

    private File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            throw new RuntimeException("Aucun fichier sélectionné !");
        }
    }
}