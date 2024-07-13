//
//import { toHTML, toMarkdownV2 } from "@telegraf/entity";


function telegramToHTML(text, json) {

        if (json.forward_origin) {
            var str = json.caption;
            var entities = json.caption_entities;
            var html = toHTML(str, entities);
            //return json.forward_origin;
            return html;
        }

    return text;

}

