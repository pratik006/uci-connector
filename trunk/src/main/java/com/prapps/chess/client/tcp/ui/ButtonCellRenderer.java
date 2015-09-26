/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.prapps.chess.client.tcp.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author pratik
 */
public class ButtonCellRenderer extends JButton implements TableCellRenderer, TableCellEditor {

	public ButtonCellRenderer() {
		System.out.println("adding action listener");
		addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("clicked"+e.getActionCommand());
				
			}
		});
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText("Restart");
        return this;
    }

	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isCellEditable(EventObject anEvent) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shouldSelectCell(EventObject anEvent) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stopCellEditing() {
		// TODO Auto-generated method stub
		return false;
	}

	public void cancelCellEditing() {
		// TODO Auto-generated method stub
		
	}

	public void addCellEditorListener(CellEditorListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeCellEditorListener(CellEditorListener l) {
		// TODO Auto-generated method stub
		
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
