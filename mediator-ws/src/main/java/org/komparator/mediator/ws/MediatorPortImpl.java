package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.mediator.ws.domain.Cart;
import org.komparator.mediator.ws.domain.Item;
import org.komparator.mediator.ws.domain.Mediator;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;

import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

@HandlerChain(file = "/mediator-ws_handler-chain.xml") 
@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
	)

public class MediatorPortImpl implements MediatorPortType{
	// end point manager
	private MediatorEndpointManager endpointManager;
	
	private MediatorClient mediatorClient = null;
	
	List<ShoppingResultView> shopList = new ArrayList<>();

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	// Main operations -------------------------------------------------------

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception{
		
		List<ItemView> result = new ArrayList<ItemView>();
		List<SupplierClient> supplierClients = GetClients();
		ItemIdView itemIdView = null;
		ItemView itemView = null;
		Item item = null;
		for(SupplierClient sc : supplierClients){
			try {
				
				ProductView product = sc.getProduct(productId);
				if(product != null){
					item = new Item(sc.getProduct(productId), sc.getWsName());
					itemIdView = newItemIdView(item);
					itemView = newItemView(itemIdView, item);
					result.add(itemView);
				}
			} catch (BadProductId_Exception e) {
				System.out.printf("Bad Product Id: %s%n", e);
			}
			
		}
		if(result!=null){
			result.sort(Comparator.comparing(ItemView::getPrice));
		}
		return result;
	}
	
	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		
		List<ItemView> result = new ArrayList<ItemView>();
		List<SupplierClient> supplierClients = GetClients();
		ItemView itemView = null;
		ItemIdView itemIdView = null;
		Item item = null;
		if(descText==null||descText.equals(""))
			throw new InvalidText_Exception("Null or empty query string", new InvalidText());
		for(SupplierClient sc : supplierClients){
			try{
				for(ProductView product : sc.searchProducts(descText)){
					item = new Item(sc.getProduct(product.getId()), sc.getWsName());
					if(item != null){
						itemIdView = newItemIdView(item);
						itemView = newItemView(itemIdView, item);
						result.add(itemView);
					}
				}
			}catch(BadText_Exception bte){
				System.out.printf("Bad description text: %s%n", bte);
			}catch(BadProductId_Exception bid){
				System.out.printf("Bad Product Id: %s%n", bid);
			}
		}
		if(result!=null){
			result.sort(Comparator.comparing(ItemView::getPrice));
		}
		return result;
	}
	
	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {

		Mediator mediator = Mediator.getInstance();
		verifyArgs(cartId, itemId, itemQty);

		if(cartId==null||cartId.equals(""))
			throw new InvalidCartId_Exception("Null or empty cartID", new InvalidCartId());
		if(itemId==null||itemId.equals("")||itemId.getProductId().equals(null)||itemId.getSupplierId().equals(null))
			throw new InvalidItemId_Exception("Null or empty itemID", new InvalidItemId());
		if(itemQty<=0)
			throw new InvalidQuantity_Exception("itemQty must be >=1", new InvalidQuantity());
		if(!mediator.cartExists(cartId)){
			mediator.registerCart(cartId);
		}
		
		int availableQty = -1;
		Boolean existingItem = false;
		List<SupplierClient> supplierClients = GetClients();
		SupplierClient supplierAux = null;
		for(SupplierClient sc : supplierClients){
			if(sc.getWsName().equals(itemId.getSupplierId())){
				try {
					if(itemId.getProductId()!=null)
						if(sc.getProduct(itemId.getProductId())!=null){
							availableQty = sc.getProduct(itemId.getProductId()).getQuantity();
							supplierAux = sc;
						}
				} catch (BadProductId_Exception e1) {
					throw new InvalidItemId_Exception("Null or empty itemID", new InvalidItemId()); 
				}
			}
		}
		if(availableQty==-1)
			throw new InvalidItemId_Exception("Null or empty itemID", new InvalidItemId());
		if(itemQty>availableQty || availableQty == 0){
			throw new NotEnoughItems_Exception("Not enough items available", new NotEnoughItems());
		} else {
			
			ItemView itemV = new ItemView();
			try {
				itemV.setDesc(supplierAux.getProduct(itemId.getProductId()).getDesc());
			} catch (BadProductId_Exception e1) {
				throw new InvalidItemId_Exception("Null or empty itemID", new InvalidItemId()); 
			}
			itemV.setItemId(itemId);
			try {
				itemV.setPrice(supplierAux.getProduct(itemId.getProductId()).getPrice());
			} catch (BadProductId_Exception e1) {
				throw new InvalidItemId_Exception("Null or empty itemID", new InvalidItemId()); 
			}
			CartItemView cartItem = new CartItemView();
			cartItem.setItem(itemV);
			cartItem.setQuantity(itemQty);
			
			for(CartItemView cItem : mediator.getCart(cartId).getItems()){
				if(cItem.getItem().getItemId().getProductId().equals(cartItem.getItem().getItemId().getProductId()) && 
						cItem.getItem().getItemId().getSupplierId().equals(cartItem.getItem().getItemId().getSupplierId())){
					existingItem = true;
					if(cItem.getQuantity()+itemQty <= availableQty){
						cItem.setQuantity(cItem.getQuantity()+itemQty);
						updateCart(cartId, cItem);
					} else throw new NotEnoughItems_Exception("Not enough items available", new NotEnoughItems());
				}
			}
			if(!existingItem){
				mediator.getCart(cartId).addItem(cartItem);
				updateCart(cartId, cartItem);
			}
			System.out.println(mediator.getCart(cartId).toString());
		}
	}
	
	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		Mediator mediator = Mediator.getInstance();
		int totalCompra = 0;
		int totalProdutos = 0;
		int totalFalhas = 0;
		CreditCardClient cardCli = null;
		ShoppingResultView result = new ShoppingResultView();
		
		if(cartId==null||cartId.equals(""))
			throw new InvalidCartId_Exception("Null or empty cartID", new InvalidCartId());
		if(creditCardNr==null||creditCardNr.equals(""))
			throw new InvalidCreditCard_Exception("Null or empty creditCardNr", new InvalidCreditCard());
		try {
			cardCli = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
		} catch (CreditCardClientException e) {
			System.out.println("Couldn't create a CC CLient, printing stackTrace:");
			e.printStackTrace();
		}
		if(!mediator.cartExists(cartId)){
			throw new InvalidCartId_Exception("Invalid Cart ID:"+cartId,new InvalidCartId());
		}
		
		if(!cardCli.validateNumber(creditCardNr)){
			throw new InvalidCreditCard_Exception("Invalid Card Number Provided", new InvalidCreditCard());
		}
		
		Cart cart = mediator.getCart(cartId);
		
		for(CartItemView item : cart.getItems()){
			for(SupplierClient cli : GetClients()){
				if(item.getItem().getItemId().getSupplierId().equals(cli.getWsName())){
					try{
						cli.buyProduct(item.getItem().getItemId().getProductId(), item.getQuantity());
						totalCompra += item.getItem().getPrice() * item.getQuantity();
						totalProdutos++;
						result.getPurchasedItems().add(item);
					}catch(BadProductId_Exception bpie){
						totalFalhas++;
						result.getDroppedItems().add(item);
					}catch( BadQuantity_Exception bqe){
						totalFalhas++;
						result.getDroppedItems().add(item);
					}catch(InsufficientQuantity_Exception insuf){
						totalFalhas++;
						result.getDroppedItems().add(item);
					}
				}
			}
		}
		result.setTotalPrice(totalCompra);
		result.setId(cartId+totalProdutos+totalFalhas+totalCompra);
		if(totalProdutos==cart.getItems().size()){
			result.setResult(Result.COMPLETE);
		} else if(totalFalhas==cart.getItems().size()){
			result.setResult(Result.EMPTY);
		} else result.setResult(Result.PARTIAL);
//		
//		if(shopList.equals(null))
//			shopList = new ArrayList<>();
		
		shopList.add(0, result);
		
		updateShopHistory(result);
		return result;
		
	}
	
    
	// Auxiliary operations --------------------------------------------------
	
	public List<SupplierClient> GetClients(){
		List<SupplierClient> clients = new ArrayList<SupplierClient>();
		String uddiURL = null;
		SupplierClient client = null;
		List<UDDIRecord> uddiRecords;
		try {
			uddiRecords = (List<UDDIRecord>)endpointManager.getUddiNaming().listRecords("T13_Supplier%%");
			for(UDDIRecord uddi : uddiRecords){
				uddiURL = endpointManager.getUddiNaming().lookup(uddi.getOrgName());
				try {
					client = new SupplierClient(uddiURL, uddi.getOrgName());
				} catch (SupplierClientException e) {
					System.out.println("MediatorPortImpl: SupplierClientException: Couldn't create a SupplierCLient: " + e.getMessage());
				}
				if(client != null)
					clients.add(client);
			}
		} catch (UDDINamingException e) {
			System.out.println("MediatorPortImpl: UDDINamingException: " + e.getMessage());
		}
		
		
		return clients;
	}
	
	@Override
	public String ping(String name) {
		String result = "";
		if (name == null || name.trim().length() == 0)
			name = "friend";
		List<SupplierClient> supplierClients = GetClients();
		for(SupplierClient sc : supplierClients){
			result = result + sc.ping(name) + " ";
		}
		return result;
	}

	@Override
	public void clear() {
		Mediator.getInstance().reset();
		List<SupplierClient> supplierClients = GetClients();
		for(SupplierClient client : supplierClients){
			client.clear();
		}
	
	}
	
	public void verifyArgs(String cartId, ItemIdView itemId, int itemQty)throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception {
		if(cartId==null||cartId.trim().equals(""))
			throw new InvalidCartId_Exception("Invalid Cart ID", new InvalidCartId());
		if(itemId==null||itemId.getProductId()==null||itemId.getProductId().trim().equals("")||itemId.getSupplierId()==null||itemId.getSupplierId().trim().equals(""))
			throw new InvalidItemId_Exception("Invalid Cart ID", new InvalidItemId());
		if(itemQty<=0)
			throw new InvalidQuantity_Exception("Invalid qty <= 0", new InvalidQuantity());
	}

	@Override
	public List<CartView> listCarts() {
		Mediator mediator = Mediator.getInstance();
		List<CartView> cvs = new ArrayList<CartView>();
		for (String cid : mediator.getCartsIDs()) {
			Cart c = mediator.getCart(cid);
			if(c != null){
				CartView cv = new CartView();
				cv.setCartId(c.getCartId());
				cv.items = new ArrayList<CartItemView>();
				cv.items = c.getItems();
				cvs.add(cv);
			}
		}
		return cvs;
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		return shopList;
	}

	
	// View helpers -----------------------------------------------------
	
    private ItemView newItemView(ItemIdView itemIdView, Item item){
    	ItemView view = new ItemView();
    	view.setItemId(itemIdView);
    	view.setDesc(item.getItemProduct().getDesc());
    	view.setPrice(item.getItemProduct().getPrice());
    	return view;
    }
    
    private ItemIdView newItemIdView(Item item){
    	ItemIdView view = new ItemIdView();
    	view.setProductId(item.getItemProduct().getId());
    	view.setSupplierId(item.getSupplierId());
    	return view;
    }

	@Override
	public void iamAlive() {
		if(endpointManager.getWsId()==1){
			return;
		}
		else {
			endpointManager.setLastAlive(new Date());
		}
	}

	@Override
	public void updateCart(String cartId, CartItemView cartItem) {
		if(endpointManager.getWsId()==1){
			try {
				mediatorClient = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
				mediatorClient.updateCart(cartId, cartItem);
			} catch (MediatorClientException e) {
				System.out.println("Error creating MediatorClient instance" + e.getMessage());
			}
		}else{
			Mediator mediator = Mediator.getInstance();
			Boolean existingItem = false;
			if(!mediator.cartExists(cartId)){
				mediator.registerCart(cartId);
			}
			for(CartItemView cItem : mediator.getCart(cartId).getItems()){
				if(cItem.getItem().getItemId().getProductId().equals(cartItem.getItem().getItemId().getProductId()) && 
						cItem.getItem().getItemId().getSupplierId().equals(cartItem.getItem().getItemId().getSupplierId())){
					existingItem = true;
					cItem.setQuantity(cartItem.getQuantity());
				}
			}
			if(!existingItem){
				mediator.getCart(cartId).addItem(cartItem);
			}
			System.out.println(mediator.getCart(cartId).toString());
		}		
	}

	@Override
	public void updateShopHistory(ShoppingResultView shoppingResult) {
		if(endpointManager.getWsId()==1){
			try {
				mediatorClient = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
				mediatorClient.updateShopHistory(shoppingResult);
			} catch (MediatorClientException e) {
				System.out.println("Error creating MediatorClient instance" + e.getMessage());
			}
		}else{
			if(shopList.equals(null))
				shopList = new ArrayList<>();
			
			shopList.add(0, shoppingResult);
		}
	}


}
