/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mm_purchases;

import best_chem.AbstractController;
import dbquerries.PMInventoryQuery;
import dbquerries.SupplierQuery;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import models.InventoryModel;
import models.UserModel;
import salesorder.SOItemsController;

/**
 * FXML Controller class
 *
 * @author Steven
 */
public class POItemsController extends AbstractController implements Initializable {
    
    @FXML
    private TextField qtyfld;

    @FXML
    private Button saveitembtn;

    @FXML
    private Button cancelbtn;

    @FXML
    private TextField inventoryidfld;
    
    @FXML
    private RadioButton descbtn;
    
    private InventoryModel item;
    
    @FXML
    private TableView<InventoryModel> inventorytable;
    
    @FXML
    private Button searchbtn;
    private int supid;
    
    private boolean isCancelled = false;
    
    
    private final PMInventoryQuery iq = new PMInventoryQuery();
    private final SupplierQuery sq = new SupplierQuery();

    @FXML
    public void saveItem(ActionEvent event) throws SQLException {
        
        System.out.println(this.inventorytable.getSelectionModel().getSelectedItem().getIdinventory());
        
        item = this.inventorytable.getSelectionModel().getSelectedItem();
        
        item.setSoh(this.getQty());
        
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelHandler(ActionEvent event) {
        this.isCancelled = true;
        
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }
    
    public InventoryModel getItem() throws SQLException{
        return item;
    }
    
    public int getQty(){
        return Integer.valueOf(this.qtyfld.getText());
    }
    
    private void searchSKU(String sku) throws SQLException{
        
        Iterator rs = null;
        if(this.descbtn.isSelected()){
            rs = iq.getInventoriesDesc(sku, this.supid);
        }else{
            rs = iq.getInventories(sku, this.supid);
        }
         
        
        this.RefreshItems(rs);
    }
    
    private void RefreshItems(Iterator rs){
        String[] arr = {"sku", "description", "uom", "wh", "poprice"};
        ObservableList<InventoryModel> data
                = FXCollections.observableArrayList();
        
        while(rs.hasNext()){
            data.add((InventoryModel)rs.next());
        }
        
        ObservableList<TableColumn<InventoryModel, ?>> olist;
        olist = (ObservableList<TableColumn<InventoryModel, ?>>) inventorytable.getColumns();

        for (int i = 0; i < olist.size(); i++) {
            olist.get(i).setCellValueFactory(
                    new PropertyValueFactory<>(arr[i])
            );
        }
        inventorytable.setItems(data);
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.inventoryidfld.setOnKeyPressed((KeyEvent event) -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                System.out.println(inventoryidfld.getText());
                try {
                    this.searchSKU(inventoryidfld.getText());
                } catch (SQLException ex) {
                    Logger.getLogger(SOItemsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        this.searchbtn.setOnAction((ActionEvent event) -> {
            System.out.println(inventoryidfld.getText());
            try {
                this.searchSKU(inventoryidfld.getText());
            } catch (SQLException ex) {
                Logger.getLogger(SOItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        
    }    

    /**
     * @return the isCancelled
     */
    public boolean IsCancelled() {
        return isCancelled;
    }

    @Override
    public void initData(UserModel user, int type) {
        super.setType(type);
        super.setGlobalUser(user);
    }
    
    public void setSupplier(int supid) throws SQLException{
        this.supid = supid;
        
        this.RefreshItems(this.iq.getInventories(supid));
    }
    
}
