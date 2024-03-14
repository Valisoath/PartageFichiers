/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import java.net.*;
import java.util.*;

/**
 *
 * @author melobk7
 */
public class VerifierConnecter {
    public boolean isConnectedToWiFi() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && networkInterface.supportsMulticast() && !networkInterface.isLoopback()) {
                    List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                        InetAddress address = interfaceAddress.getAddress();
                        if (address instanceof Inet4Address && !address.isLinkLocalAddress()) {
                            // If it's an IPv4 address and not a link local address, it's probably a WiFi interface
                            return true;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }
}
