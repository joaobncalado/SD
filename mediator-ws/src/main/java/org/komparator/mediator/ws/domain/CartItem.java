package org.komparator.mediator.ws.domain;

import org.komparator.mediator.ws.ItemView;

public class CartItem {
	private ItemView item;
	private int quantity;
	
	public CartItem(ItemView item, int quantity){
		this.item=item;
		this.quantity=quantity;
		
	}
	
	public ItemView getItem(){
		return this.item;
	}
	
	public int getQuantity(){
		return this.quantity;
	}
	
	public void addQuantity(int quantityToAdd){
		this.quantity=this.quantity+quantityToAdd;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CartItem [Item=").append(item.toString());
		builder.append(", quantity=").append(quantity);
		builder.append("]");
		return builder.toString();
	}
}
