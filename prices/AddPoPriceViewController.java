/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prices;

import best_chem.AbstractController;
import dbquerries.InventoryQuery;
import dbquerries.PMInventoryQuery;
import java.net.URL;
import java.sql.Date;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import models.InventoryModel;
import models.PricesModel;
import models.SupplierModel;
import models.UserModel;

/**
 * FXML Controller class
 *
 * @author Steven
 */
public class AddPoPriceViewController extends AbstractController implements Initializable {
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.skufld.setOnKeyPressed((KeyEvent event) -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                System.out.println(skufld.getText());
                try {
                    this.searchSKU(skufld.getText());
                } catch (SQLException ex) {
                    Logger.getLogger(InventoryModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        this.searchbtn.setOnAction((ActionEvent event) -> {
            System.out.println(skufld.getText());
            try {
                this.searchSKU(skufld.getText());
            } catch (SQLException ex) {
                Logger.getLogger(InventoryModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
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
    
    @FXML
    private TableView<InventoryModel> inventorytable;

    @FXML
    private Button searchbtn;
    
    @FXML
    private TextField popfld;

    @FXML
    private TextField skufld;

    @FXML
    private Button cancelbtn;

    @FXML
    private Button addbtn;
    
    @FXML
    private RadioButton descbtn;
    
    private SupplierModel sup_og;
    
    @FXML
    private DatePicker effdtefld;
    
    private final InventoryQuery iq = new InventoryQuery();
    private final PMInventoryQuery pmiq = new PMInventoryQuery();

    @FXML
    public void cancelHandler(ActionEvent event) {
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void AddHandler(ActionEvent event) throws SQLException {
        PricesModel price = new PricesModel();
        
        price.setIdinventory(this.inventorytable.getSelectionModel().getSelectedItem().getIdinventory());
        price.setPoprice(Double.parseDouble(this.popfld.getText()));
        price.setEffdte(Date.valueOf(this.effdtefld.getValue()));
        
        pmiq.addPriceSupplier(price, this.sup_og.getSupid());
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }
    
    public void setSupplier(SupplierModel sup){
        this.sup_og = sup;
    }

    @Override
    public void initData(UserModel user, int type) {
        super.setGlobalUser(user);
        super.setType(type);
    }
}
