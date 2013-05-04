console.log('This would be the main JS file.');
var ua = navigator.userAgent.toLowerCase();
var isAndroid = ua.indexOf("android") > -1;
/**if(isAndroid) {
 window.location.href = 'http://reinoslokos.no-ip.org/com.melchor629.musicote.apk';
} Revisar esto**/
function commit_history() {
	$.getJSON('https://api.github.com/repos/melchor629/Musicote-Melchor629/commits?callback=?', function(json) {
		$('#commit-history-json tr').remove();
		$.each(json.data, function(i, data) {
			var $col = $('<tr style="border-bottom: 1px solid #999; text-shadow: none" />');
			var $committer = $('<td valign="top" />').html(data.commit.committer.name);
			var $link = $('<a style="font-weight: bold" />').attr('href', 'https://github.com/needim/noty/commit/' + data.sha).html(data.commit.message);
			var $url = $('<td />').append($link);
			var $date = $('<td style="text-align: right" />').html($.format.date(data.commit.committer.date, "dd.MM.yy HH:MM"));
			
			$col.append($committer);
			$col.append($url);
			$col.append($date);
			
			$('#commit-history-json').append($col);
		});
	});
}
