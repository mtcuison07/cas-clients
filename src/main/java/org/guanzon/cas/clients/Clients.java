/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.clients;

/**
 *
 * @author User
 */
public class Clients {
    
    
    private ClientTypes type;
    
    public ClientTypes getType() {
        return type;
    }

    public Clients(ClientTypes type) {
        this.type = type;
    }
    
    public void displayInfo() {
        System.out.println("Type: " + type);
    }
}
