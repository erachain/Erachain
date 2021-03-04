package org.erachain.core.blockexplorer;

import org.json.simple.JSONObject;

public interface ExplorerJsonLine {

    JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args);

}
