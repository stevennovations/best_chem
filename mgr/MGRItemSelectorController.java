/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mgr;

import best_chem.AbstractController;
import dbquerries.InventoryQuery;
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
public class MGRItemSelectorController extends AbstractController implements Initializable {
    
    @FXML
    private TextField qtyfld;

    @FXML
    private TableView<InventoryModel> inventorytable;

    @FXML
    private Button saveitembtn;

    @FXML
    private Button searchbtn;

    @FXML
    private Button cancelbtn;

    @FXML
    private TextField inventoryidfld;
    
    @FXML
    private RadioButton descbtn;
    
    private InventoryModel item;
    
    private boolean isCancelled = false;
    
    private final InventoryQuery iq = new InventoryQuery();
    
    @FXML
    public void additem(ActionEvent event) throws SQLException {
        
        this.item = iq.getInventory(this.inventorytable.getSelectionModel().getSelectedItem().getIdinventory(), super.getType());
        System.out.println(this.item.getIdinventory());
        System.out.println(this.item.getDescription());
        this.item.setSoh(Integer.valueOf(this.qtyfld.getText()));
        Stage stage = (Stage) saveitembtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelHandler(ActionEvent event) {
        this.isCancelled = true;
        
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }
    
    private void searchSKU(String sku) throws SQLException{

        String[] arr = {"sku", "description", "uom", "wh", "soh", "csl"};
        ObservableList<InventoryModel> data
                = FXCollections.observableArrayList();
        
        Iterator rs = null;
        
        if(this.descbtn.isSelected()){
            rs = iq.getInventoriesByDesc1(sku, super.getType());
        }else{
            rs = iq.getInventories(sku, super.getType());
        }
        
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
    
    public InventoryModel getitem(){
        return item;
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
    }
    
}
