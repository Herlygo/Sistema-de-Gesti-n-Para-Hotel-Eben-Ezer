package com.mycompany.apphotel;

import com.hotel.vista.VentanaLogin;
import javax.swing.SwingUtilities;

public class AppHotel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
    
    
}