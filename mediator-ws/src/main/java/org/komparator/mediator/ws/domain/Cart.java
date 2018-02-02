package org.komparator.mediator.ws.domain;

import java.util.ArrayList;
import java.util.List;

import org.komparator.mediator.ws.CartItemView;

public class Cart {
	private List<CartItemView> items = new ArrayList<CartItemView>();
	private String cartId;
	
	public Cart(String cartId) {
		this.cartId=cartId;
	}
	
	public String getCartId(){
		return this.cartId;
	}
	
	public List<CartItemView> getItems(){
		return this.items;
	}
	
	public void addItem(CartItemView item){
		this.items.add(item);
	}
	
	@Override
	public String toString(){
		StringBuilder description=new StringBuilder();
		description.append("Cart ID = " + this.cartId);
		description.append(", Products: \n");
		items.forEach(item->{
			description.append("|");
			description.append("ProductID: " + item.getItem().getItemId().getProductId());
			description.append(", Qty: " + item.getQuantity());
			description.append(", Supplier: " + item.getItem().getItemId().getSupplierId());
			description.append("|\n");
		});
		return description.toString();
	}

}
