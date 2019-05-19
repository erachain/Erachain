package org.erachain.core.blockexplorer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface ExplorerJsonLine {

    abstract JSONObject jsonForExolorerPage(JSONObject langObj);

    }
