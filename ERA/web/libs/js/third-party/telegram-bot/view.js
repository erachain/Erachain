// Функция для экранирования специальных символов

const map = {
    '\n': '<br>',
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
};

function escapeHtml(text) {
    return text.replace(/[&<>"'`\n=/]/g, function(m) { return map[m]; });
}

const priority = {
    'blockquote': 0,
    'pre': 11,
    'code': 20,
    'text_link': 49,
    'text_mention': 49,
    'mention': 50,
    'hashtag': 50,
    'bot_command': 50,
    'url': 50,
    'email': 50,
    'cashtag': 50,
    'phone_number': 50,
    'bold': 90,
    'italic': 91,
    'underline': 92,
    'strikethrough': 93,
    'spoiler': 94,
    'custom_emoji': 99
};

// Функция для сортировки сущностей по приоритету
function sortEntities(entities) {

    return entities.sort((a, b) => {
        if (a.offset < b.offset) return -1;
        if (a.offset > b.offset) return 1;
        if (a.length > b.length) return -1;
        if (a.length < b.length) return 1;
        if (priority[a.type] < priority[b.type]) return -1;
        if (priority[a.type] > priority[b.type]) return 1;
        return 0;
    });
}

function convertEntity(text, entity) {
    switch (entity.type) {
        case 'bold':
            return `<b>${text}</b>`;
        case 'italic':
            return `<i>${text}</i>`;
        case 'underline':
            return `<u>${text}</u>`;
		case "strikethrough":
            return `<del>${text}</del>`;
		case "code":
            return `<code>${text}</code>`;
		case "pre":
			if (entity.language)
                return `<pre><code class="language-${entity.language}">${text}</code></pre>`;
            else
                return `<pre>${text}</pre>`;
		case "spoiler":
            return `<p class="tgm spoiler">${text}</p>`;
		case "url":
            return `<a class="tgm text" href="${entity.text}" target="_blank">${text}</a>`;

         // <a href="https://t.me/rt_russian/208358" target="_blank" rel="noopener" onclick="return confirm('Open this link?\n\n'+this.href);"><b>упал</b></a>
        case 'text_link':
            return `<a class="tgm text" href="${entity.url}" target="_blank">${text}</a>`;

        case 'blockquote':
            return `<blockquote class="tgm" style="background-color:seashell; border-left: 5px solid burlywood;">${text}</blockquote>`;

// <i class="emoji" style="background-image:url('//telegram.org/img/emoji/40/F09F9FA9.png')"><b>🟩</b></i>
        case 'custom_emoji':
            // не дает сейчас по ссылке иконки - их нужно в бот по токену загружать и то они живут не долго
            // см. getFile и getCustomEmojiStickers
            // return `<img src="https://t.me/iv?url=${entity.custom_emoji_id}">`;
            return text;
		case "text_mention":
            return `<a class="tgm text" href="tg://user?id=${entity.user.id}" target="_blank">${text}</a>`;

// тут не правильнрый код
		case "mention":
		case "custom_emoji":
		case "hashtag":
		case "cashtag":
		case "bot_command":
		case "phone_number":
		case "email":
            return `<a class="tgm mention" href="https://t.me/${text.slice(1)}" target="_blank">${text}</a>`;
        default:
            return `<blockquote class="tgm field" style="background-color:lavender; border-left: 5px solid powderblue;">${text}</blockquote>`;
    }
}

// https://t.me/rt_russian/207494
function parseEntities(text, startPos, endPos, entities) {
    if (entities.length === 0)
        return [escapeHtml(text.slice(startPos, endPos)), []];

    var entity = entities[0];
    if (entity.offset >= endPos) {
        // это начался новый верхний уровень - выход из глубины
        return [escapeHtml(text.slice(startPos, endPos)), entities];
    }

    var remainingEntities = entities;
    var levelText = "";

    if (entity.offset != startPos)
        // участок без форматирования
        levelText += escapeHtml(text.slice(startPos, entity.offset));

	do {
        // обработка на этом уровне других
		// идем глубже - это вложенный формат

        var [wrappedText, remainingEntities] = parseEntities(text, entity.offset, entity.offset + entity.length, remainingEntities.slice(1));
        levelText += convertEntity(wrappedText, entity);
        if (remainingEntities.length === 0)
            break;

        var entityLevel = remainingEntities[0];
        if (entityLevel.offset >= endPos)
            break;

        if (entity.offset + entity.length != entityLevel.offset)
            // участок без форматирования
            levelText += escapeHtml(text.slice(entity.offset + entity.length, entityLevel.offset));

        entity = entityLevel;

	} while (remainingEntities.length > 0
            && entityLevel.offset < endPos);

    if (entity.offset + entity.length != endPos)
        // участок без форматирования
        levelText += escapeHtml(text.slice(entity.offset + entity.length, endPos));

    return [levelText, remainingEntities];
}

function copyToClipboard(data) {
    const jsonStr = decodeURIComponent(data);
    const el = document.createElement('textarea');
    el.value = jsonStr;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
    alert('Copied to clipboard');
}

function telegramView(text_in, json) {

    var text;
    var entities;
    if (json.caption) {
        text = json.caption;
        entities = json.caption_entities;
    } else {
        text = json.text;
        entities = json.entities;
    }

    var formattedText;
    if (entities) {
        entities = sortEntities(entities);
        var [formattedText, _] = parseEntities(text, 0, text.length, entities);
    } else {
        formattedText = escapeHtml(text);
    }

    var messageChat;
    var messageUrl;

    if (json.forward_origin) {
        messageChat = json.forward_origin;
    }

    if (messageChat.chat.username) {
        messageUrl = "<a href=https://t.me/" + messageChat.chat.username + "/" + messageChat.message_id + " target=\"_blank\">" + messageChat.chat.title + "</a>";
    }


    return `
        <div class=row style="border: 2px solid #ccc; background-color: ghostwhite;"><div class=col-xs-12>
            <div class=row style="line-height:1.5em; padding-bottom: 10px;"><div class="col-lg-2 col-md-1"></div><div class="col-lg-8 col-md-10 col-xs-12">
                <div class=row style="font-size: 1.4em"><div class=col-xs-11 style="padding-top: 0.3em"><b>${messageUrl}</b></div><div class=col-xs-1><a class="button ll-blue-bgc glyphicon glyphicon-copy" onclick="copyToClipboard('${encodeURIComponent(text_in)}')"></a></div></div>
                <div class=row ><div class=col>${formattedText}</div></div>
            </div><div class="col-lg-2 col-md-1"></div></div>
        </div></div>
    `;
}
