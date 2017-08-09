/* Preview uploaded image */
function readURL(input) {

	if (input.files && input.files[0]) {
	    var reader = new FileReader();
	    var file = input.files[0];
	    var imageType = /^image\//;
	    var img;
	
		    // test if uploaded file is an image
		    if (!imageType.test(file.type))  return

		    // create the image if not exists
		    if (! $("#" + input.id +"_preview").length) { 
			img = $("<img id='"+ input.id +"_preview' class='img-thumbnail' src='#' ></img>");
			$("#" + input.id ).after( $("<p></p>").append( img ) );
		    } else {
			img = $("#" + input.id +"_preview");
		    }


	    	var reader = new FileReader();
    		reader.onload = function(e) { img.attr('src', e.target.result); };
    		reader.readAsDataURL(file);
	}
}


$(document).ready(function(){
    
    /* add image preview behaviour */
    $("#file").change(function(){
        readURL(this);
    });

});


