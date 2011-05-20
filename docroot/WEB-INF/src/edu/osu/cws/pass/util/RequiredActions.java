package edu.osu.cws.pass.util;


import edu.osu.cws.pass.models.Appraisal;
import edu.osu.cws.pass.models.PermissionRule;
import edu.osu.cws.pass.models.RequiredAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequiredActions {
//
//    private HashMap permissionRuleMap;
//
//    private HashMap appraisalStepMap;
//
//    public List getAppraisalActions(List<Appraisal> appraisalList, String role) {
//        ArrayList<RequiredAction> outList = new ArrayList<RequiredAction>();
//        String actionKey = "";
//        RequiredAction actionReq;
//        HashMap anchorParams;
//
//        for (Appraisal appraisal : appraisalList) {
//          	//get the status, compose the key "status"-"role"
//            actionKey = appraisal.getStatus()+"-"+role;
//
//            // Get the appropriate permissionrule object from the permissionRuleMap
//            PermissionRule rule = (PermissionRule) permissionRuleMap.get(actionKey);
//            if (rule.getActionRequired() != null || rule.getActionRequired() != "") {
//                // compose a requiredAction object and add it to the outList.
//                anchorParams = new HashMap();
//                anchorParams.put("action", rule.getActionRequired());
//                anchorParams.put("id", appraisal.getId());
//
//                actionReq = new RequiredAction();
//                actionReq.setParameters(anchorParams);
//
//
//            }
//
//
//        }
////o	if permissions.getaActionRequired is not empty
////•	Using the action key in the permissonRule to get the anchor text from the resource bundle and format it with the variables.
////•	Map should include two parameters:
////o	action=displayAppraisal
////o	id=<appraisalID>
////•	Return outList
//
//        return new ArrayList();
//
//    }
//
//    public void setPermissionRuleMap(HashMap permissionRuleMap) {
//        this.permissionRuleMap = permissionRuleMap;
//    }
//
//    public void setAppraisalStepMap(HashMap appraisalStepMap) {
//        this.appraisalStepMap = appraisalStepMap;
//    }
}
