<?php
if($_GET) {
    if(isset($_GET["url"]) && isset($_GET["width"]) && isset($_GET["height"])) {
        $url = urldecode($_GET["url"]);
        $width = intval($_GET["width"]);
        $height = intval($_GET["height"]);

        //Finds out if the file is a PNG file or JPG file
        if(strstr($url, ".png") != null) {
            $ilastfm = imagecreatefrompng($url);
            imagesavealpha($ilastfm, true);
            imageAlphaBlending($ilastfm, false);
        } elseif(strstr($url, ".jpg") != null) {
            $ilastfm = imagecreatefromjpeg($url);
            imagesavealpha($ilastfm, true);
            imageAlphaBlending($ilastfm, false);
        } else {
            $ilastfm = imagecreate(600, 600);
            imagesavealpha($ilastfm, true);
            imageAlphaBlending($ilastfm, false);
            imagefill($ilastfm, 0, 0, imagecolorallocatealpha($ilastfm, 200,200,200,127));
        }

        //Creates the final image
        $inew = imagecreatetruecolor($width, $height);
        imagesavealpha($inew, true);
        imageAlphaBlending($inew, false);
        imagefill($inew, 0, 0, imagecolorallocatealpha($inew, 200,200,200,127));

        //The album image scaled
        $iscaled = imagecreatetruecolor($width, $width);
        imagesavealpha($iscaled, true);
        imageAlphaBlending($iscaled, false);
        //The image frame
        $imarco = imagecreatefrompng("marco.png");
        imagesavealpha($imarco, true);
        imageAlphaBlending($imarco, false);

        //The image frame scaled
        $imarco2 = imagecreatetruecolor($width, $height);
        imagesavealpha($imarco2, true);
        imageAlphaBlending($imarco2, false);
        $w = imagesx($ilastfm);
        $w2 = 720;
        $h = imagesy($ilastfm);
        $h2 = 1280;

        //Scale images
        imagecopyresampled($iscaled, $ilastfm, 0, 0, 0, 0, $width, $width, $w, $h);
        imagecopyresampled($imarco2, $imarco, 0, 0, 0, 0, $width, $height, $w2, $h2);
        $dst_x = ($width - $width) / 2;
        $dst_y = ($height - $width) / 2;
        //Mergering images, first the album and then the frame
        imagecopymerge($inew, $iscaled, $dst_x, $dst_y, 0, 0, $w, $h, 75);
        imageAlphaBlending($inew, true);
        imagesavealpha($inew, true);
        imagecopymerge_alpha($inew, $imarco2, 0, 0, 0, 0, $w2, $h2, 100);

        //Prints the image and destroy the images
        header("Content-Type: image/png");
        imagepng($inew);
        imagedestroy($ilastfm);
        imagedestroy($inew);
        imagedestroy($isacaled);
        imagedestroy($imarco);
        imagedestroy($imarco2);
    } else {
        $inew = imagecreatetruecolor(1, 1);
        imagepng($inew);
        imagedestroy($inew);
    }
}

function imagecopymerge_alpha($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h, $pct){ 
    if(!isset($pct)){ 
        return false; 
    } 
    $pct /= 100; 
    // Get image width and height 
    $w = imagesx( $src_im ); 
    $h = imagesy( $src_im ); 
    // Turn alpha blending off 
    imagealphablending( $src_im, false ); 
    // Find the most opaque pixel in the image (the one with the smallest alpha value) 
    $minalpha = 127; 
    for( $x = 0; $x < $w; $x++ ) 
    for( $y = 0; $y < $h; $y++ ){ 
        $alpha = ( imagecolorat( $src_im, $x, $y ) >> 24 ) & 0xFF; 
        if( $alpha < $minalpha ){ 
            $minalpha = $alpha; 
        } 
    } 
    //loop through image pixels and modify alpha for each 
    for( $x = 0; $x < $w; $x++ ){ 
        for( $y = 0; $y < $h; $y++ ){ 
            //get current alpha value (represents the TANSPARENCY!) 
            $colorxy = imagecolorat( $src_im, $x, $y ); 
            $alpha = ( $colorxy >> 24 ) & 0xFF; 
            //calculate new alpha 
            if( $minalpha !== 127 ){ 
                $alpha = 127 + 127 * $pct * ( $alpha - 127 ) / ( 127 - $minalpha ); 
            } else { 
                $alpha += 127 * $pct; 
            } 
            //get the color index with new alpha 
            $alphacolorxy = imagecolorallocatealpha( $src_im, ( $colorxy >> 16 ) & 0xFF, ( $colorxy >> 8 ) & 0xFF, $colorxy & 0xFF, $alpha ); 
            //set pixel with the new color + opacity 
            if( !imagesetpixel( $src_im, $x, $y, $alphacolorxy ) ){ 
                return false; 
            } 
        } 
    } 
    // The image copy 
    imagecopy($dst_im, $src_im, $dst_x, $dst_y, $src_x, $src_y, $src_w, $src_h); 
} 
