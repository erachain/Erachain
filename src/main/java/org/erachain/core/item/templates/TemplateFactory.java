package org.erachain.core.item.templates;

public class TemplateFactory {

    private static TemplateFactory instance;

    private TemplateFactory() {

    }

    public static TemplateFactory getInstance() {
        if (instance == null) {
            instance = new TemplateFactory();
        }

        return instance;
    }

    public TemplateCls parse(int forDeal, byte[] data, boolean includeReference) throws Exception {
        //READ TYPE
        int type = data[0];

        switch (type) {
            case TemplateCls.PLATE:

                //PARSE SIMPLE PLATE
                return Template.parse(forDeal, data, includeReference);

            case TemplateCls.SAMPLE:

                //
                //return Template.parse(data, includeReference);

            case TemplateCls.PAPER:

                //
                //return Template.parse(data, includeReference);
        }

        throw new Exception("Invalid Template type: " + type);
    }

}
