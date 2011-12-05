

(function($){
$.smtf = function(selector, col_sel){
	$(selector).find("thead:first th").each(function(index){
		$(this).append("</br><input type='text' size='8' name= '"+index+"' class=filterText />");});	
	$('input.filterText').keyup(function(event) {
		if (event.which == 13){
			var typed_val = $(this).val();
			var counter = 0
			$('input.filterText').each(function(){
				var col_ind = Number($(this).attr("name"));
				var inp_val = $(this).val();
				var col_pos = col_ind + 1;
		
			if(inp_val == ""){
				counter=counter+1
				if (counter==col_sel.length){
					$('#_table>tbody>tr>td:nth-child('+col_pos+')').each(function(){
						$(this).parent().removeClass();
						$(this).parent().addClass("showRow");
					});
				};
			};

			if (inp_val !=""){	
				if(col_sel[col_ind]=='number'){
					$('#_table>tbody>tr>td:nth-child('+col_pos+')').each(function(){
						var typed_l = inp_val.split(" ");
						switch (typed_l[0])
						{
							case ">":
								if (parseFloat(typed_l[1]) > parseFloat($(this).text()) ){
									$(this).parent().addClass("hideRow")};
								break;
							case "<":
								if (parseFloat(typed_l[1]) < parseFloat($(this).text()) ){
									($(this).parent().addClass("hideRow"))};
								break;
							case "=":
								if (parseFloat(typed_l[1]) == parseFloat($(this).text()) ){
									$(this).parent().addClass("showRow");}
								else {$(this).parent().addClass("hideRow")};
								break;
							default:
								$(this).parent().removeClass("hideRow");


						};
					});
				}
				else {
					var rows = $('#_table>tbody>tr');
					rows.find('td:nth-child('+col_pos+'):not(:contains("'+inp_val+'"))').parent().addClass("hideRow");
					rows.find('td:nth-child('+col_pos+'):contains("'+inp_val+'")').parent().addClass("showRow");
				};
			};
		});
			//$("#_table>tbody>tr").show();
			$(".hideRow").hide().removeClass();
			$(".showRow").show().removeClass();
		};
	});
return this;	
};
})(jQuery);

