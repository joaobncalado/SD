package org.komparator.mediator.ws.domain;

import org.komparator.supplier.ws.ProductView;

public class Item {
	private ProductView product;
	private String supplierID;
	
	public Item(ProductView prod, String supplier) {
		this.product=prod;
		this.supplierID=supplier;
	}
	
	public String getSupplierId(){
		return this.supplierID;
	}
	
	public ProductView getItemProduct(){
		return this.product;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [SupplierId=").append(supplierID);
		builder.append(", [ProductId=").append(product.getId());
		builder.append(", productQt=").append(product.getQuantity());
		builder.append(", productDesc=").append(product.getDesc());
		builder.append(", productPrice=").append(product.getPrice());
		builder.append("]]");
		return builder.toString();
	}
	
}
