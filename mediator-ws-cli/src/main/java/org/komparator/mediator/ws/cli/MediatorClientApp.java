package org.komparator.mediator.ws.cli;

import java.util.List;

import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit
        
        System.out.println("Invoke ping()...");
        String result = client.ping("client");
        System.out.println(result);
        
        
        List<ItemView> items = null;
        
        ProductView prod = new ProductView();
		prod.setId("p1");
		prod.setDesc("AAA bateries (pack of 3)");
		prod.setPrice(3);
		prod.setQuantity(10);
		
		ProductView prod2 = new ProductView();
		prod2.setId("p2");
		prod2.setDesc("10x AAA battery");
		prod2.setPrice(8);
		prod2.setQuantity(20);
		
		ProductView prod3 = new ProductView();
		prod3.setId("p3");
		prod3.setDesc("Digital Multimeter");
		prod3.setPrice(15);
		prod3.setQuantity(5);
		
		ProductView prod4 = new ProductView();
		prod4.setId("p4");
		prod4.setDesc("very cheap batteries");
		prod4.setPrice(2);
		prod4.setQuantity(5);
		
		System.out.println("Created products, creating supplierclint: ");
		SupplierClient sc = new SupplierClient(uddiURL, "T13_Supplier1");
		sc.createProduct(prod);
		sc.createProduct(prod2);
		sc.createProduct(prod3);
		sc.createProduct(prod4);
		System.out.println("Created supplierCLient to insert test products: ");
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId("T13_Supplier1");
		
		System.out.println("Going to add to cart: ");
		client.addToCart("xyz", id, 1);
		System.out.println("Added to cart prod...");
        
        items = client.getItems("p1");
        for(ItemView it : items){
        	System.out.println(it.getDesc());
        }
        System.out.println("Ending app");
    }
}
