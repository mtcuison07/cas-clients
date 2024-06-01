package org.guanzon.cas.clients.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.iface.GTransaction;
import org.guanzon.cas.model.clients.ar.Model_AR_Client_Ledger;
import org.guanzon.cas.models.Model_Account_Accreditation;
import org.guanzon.cas.validators.ValidatorFactory;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard
 */
public class Account_Accreditations implements GTransaction {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    String psTransNox;
    Model_Account_Accreditation poModel;
    JSONObject poJSON;
    public String getTransNox(){
        return psTransNox;
    }
    public Account_Accreditations(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        pnEditMode = EditMode.UNKNOWN;
    }
    
    @Override
    public JSONObject newTransaction() {
        poJSON = new JSONObject();
        try{
            poModel = new Model_Account_Accreditation(poGRider);
            poModel.newRecord();
            
            poJSON.put("result", "success");
            poJSON.put("message", "initialized new record.");
            pnEditMode = EditMode.ADDNEW;
               
        }catch(NullPointerException e){
            
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        
        return poJSON;
    }

    @Override
    public JSONObject openTransaction(String fsValue) {
    
        pnEditMode = EditMode.READY;
        poJSON = new JSONObject();
        
        poJSON = poModel.openRecord(fsValue);
        return poJSON;
    }

    @Override
    public JSONObject updateTransaction() {
    
         poJSON = new JSONObject();
        
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE){
            poJSON.put("result", "success");
            poJSON.put("message", "Edit mode has changed to update.");
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }

        return poJSON;
    }

    @Override
    public JSONObject saveTransaction() {
        poJSON = new JSONObject();
        if (!pbWthParent) {
            poGRider.beginTrans();
        }

       
        ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, poModel);
        poModel.setModifiedDate(poGRider.getServerDate());

        if (!validator.isEntryOkay()){
            poJSON.put("result", "error");
            poJSON.put("message", validator.getMessage());
            return poJSON;

        }
        poJSON = poModel.saveRecord();  
        if ("success".equals((String)poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
        }

        return poJSON;
    }

    @Override
    public JSONObject deleteTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject closeTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY || getEditMode() == EditMode.UPDATE) {
            
            poJSON = poModel.setTranStatus(TransactionStatus.STATE_CLOSED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject postTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            poJSON = poModel.setTranStatus(TransactionStatus.STATE_POSTED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject voidTransaction(String string) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            poJSON = poModel.setTranStatus(TransactionStatus.STATE_VOID);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
//
            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject cancelTransaction(String fsTransNox) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            
            
            poJSON = poModel.setTranStatus(TransactionStatus.STATE_CANCELLED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }


    @Override
    public JSONObject searchWithCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject searchTransaction(String fsColumn, String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";

        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition = fsColumn + " IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = fsColumn + " = " + SQLUtil.toSQL(psTranStatus);
        }

        if (!fbByCode) {
            lsFilter = fsColumn + " LIKE " + SQLUtil.toSQL(fsValue);
        } else {
            lsFilter = fsColumn + " = " + SQLUtil.toSQL(fsValue);
        }
        Model_Account_Accreditation model = new Model_Account_Accreditation(poGRider);
        String lsSQL = MiscUtil.addCondition(model.makeSQL(), lsCondition + " AND " + lsFilter + " GROUP BY sTransNox");
        
        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Date»Name",
                "sTransNox»dTransact»sCompnyNm",
                "sTransNox»dTransact»sCompnyNm",
                fbByCode ? 0 : 1);

        if (poJSON != null) {
//            return poModel.openRecord((String) poJSON.get("sTransNox"));
            return openTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
            return poJSON;
        }
    }

    @Override
    public JSONObject searchMaster(String string, String string1, boolean bln) {
        return searchMaster(poModel.getColumn(string), string1, bln);
    }

    @Override
    public JSONObject searchMaster(int i, String string, boolean bln) {
        poJSON = new JSONObject();
        switch(i){
            case 1:
               poJSON = SearchClient(string, bln);
               break;
            case 2: 
                poJSON =SearchCategory(string, bln);
                break;
        }
        return poJSON;
    }

    @Override
    public Model_Account_Accreditation getMasterModel() {
        return poModel;
    }


    
    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        if (pnEditMode != EditMode.UNKNOWN){
            // Don't allow specific fields to assign values
            if(!(fnCol == poModel.getColumn("sClientID") ||
                fnCol == poModel.getColumn("cRecdStat") ||
                fnCol == poModel.getColumn("sModified") ||
                fnCol == poModel.getColumn("dModified"))){
                
                obj = poModel.setValue(fnCol, foData);
            }
        }
        return obj;
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return setMaster(poModel.getColumn(fsCol), foData);
    }

    public Object getMaster(int fnCol) {
        if(pnEditMode == EditMode.UNKNOWN)
            return null;
        else 
            return poModel.getValue(fnCol);
    }

    public Object getMaster(String fsCol) {
        return getMaster(poModel.getColumn(fsCol));
    }


    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public void setTransactionStatus(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
 
    public JSONObject SearchCategory(String fsValue, boolean fbByCode){
        String lsHeader = "ID»Category";
        String lsColName = "sCategrCd»sDescript»";
        String lsColCrit = "sCategrCd»sDescript";
        String lsTable;
        lsTable = "Category";
        String lsSQL = " SELECT " +
                            " sCategrCd, " +
                            " sDescript, " +
                            " sInvTypCd, " +
                            " cRecdStat " +
                      " FROM " + lsTable;
        
       
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sCategrCd = " + SQLUtil.toSQL(fsValue)) + " GROUP BY sCategrCd";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "sDescript LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY sCategrCd";
        
       
      
        JSONObject loJSON;
        String lsValue;
        System.out.println("lsSQL = " + lsSQL);
        loJSON = ShowDialogFX.Search(poGRider, 
                                        lsSQL, 
                                        fsValue, 
                                        lsHeader, 
                                        lsColName, 
                                        lsColCrit, 
                                        fbByCode ? 0 :1);
            
        System.out.println("loJSON = " + loJSON.toJSONString());
            
            if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
                System.out.println("json sCategrCd = " + (String) loJSON.get("sCategrCd"));
                lsValue = (String) loJSON.get("sCategrCd");
                setMaster("sCategrCd", (String) loJSON.get("sCategrCd"));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No Category information found for: " + fsValue + ", Please check Catergory Code and Description details.");
                return loJSON;
            }
        return loJSON;
    }
    public JSONObject SearchClient(String fsValue, boolean fbByCode){
        String lsHeader = "ID»Name»Contact Person";
        String lsColName = "sClientID»sCompnyNm»sCPerson1";
        String lsColCrit = "a.sClientID»b.sCompnyNm»c.sCPerson1";
        String lsTable;
        if(poModel.getAcctType().equalsIgnoreCase("0")){
            lsTable = "AP_Client_Master";
        }else{
            lsTable = "AR_Client_Master";
        }
        String lsSQL = "SELECT " +
                            "  a.sClientID" +
                            ", b.sCompnyNm" +
                            ", c.sCPerson1" + 
                            ", c.sContctID" + 
                            " FROM " + lsTable + " a " +
                                " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID"+
                                " LEFT JOIN Client_Institution_Contact_Person c" +
                                    " ON b.sClientID = c.sClientID AND c.cPrimaryx = '1'";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue)) + " GROUP BY a.sClientID";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY a.sClientID";
        
       
      
        JSONObject loJSON;
        String lsValue;
        System.out.println("lsSQL = " + lsSQL);
        loJSON = ShowDialogFX.Search(poGRider, 
                                        lsSQL, 
                                        fsValue, 
                                        lsHeader, 
                                        lsColName, 
                                        lsColCrit, 
                                        fbByCode ? 0 :1);
            
        System.out.println("loJSON = " + loJSON.toJSONString());
            
            if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
                System.out.println("json sClientID = " + (String) loJSON.get("sClientID"));
                lsValue = (String) loJSON.get("sClientID");
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sContctID", (String) loJSON.get("sContctID"));
                setMaster("sCPerson1", (String) loJSON.get("sCPerson1"));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                
//                System.out.println("get sClientID = " + getAccount(fnRow, 4));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
    }
    
    private JSONObject saveRecord(String type){
        
        JSONObject obj = new JSONObject();
        if (poModel == null){
            obj.put("result", "error");
            obj.put("message", "No client address detected. Please encode client address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        Model_Account_Accreditation loModel = new Model_Account_Accreditation(poGRider);
        String lsTransNox = MiscUtil.getNextCode(loModel.getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode());        
        if("Cancel".equals(type)){
                obj = poModel.setTranStatus(TransactionStatus.STATE_CANCELLED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Void".equals(type)){
                obj = poModel.setTranStatus(TransactionStatus.STATE_VOID);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
            }else if("Post".equals(type)){
                obj = poModel.setTranStatus(TransactionStatus.STATE_POSTED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Close".equals(type)){
                obj = poModel.setTranStatus(TransactionStatus.STATE_CLOSED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, poModel);
            poModel.setModifiedDate(poGRider.getServerDate());
            poModel.setTransactionNo(lsTransNox);
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = poModel.saveRecord();  
        
        return obj;
    }
}
