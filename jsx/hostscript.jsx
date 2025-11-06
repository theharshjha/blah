
function saveSelectionAsLayer() {
    try {
        var desc = new ActionDescriptor();
        var ref = new ActionReference();
        ref.putClass( charIDToTypeID("Lyr ") );
        desc.putReference( charIDToTypeID("null"), ref );
        var desc2 = new ActionDescriptor();
        desc2.putString( charIDToTypeID("Nm  "), "Temp Selection Layer" );
        desc.putObject( charIDToTypeID("Usng"), charIDToTypeID("Lyr "), desc2 );
        executeAction( charIDToTypeID("Mk  "), desc, DialogModes.NO );
        return JSON.stringify({ success: true });
    } catch (e) {
        return JSON.stringify({ error: e.toString() });
    }
}

// Call the Flux Kontext API
function callFluxKontextAPI(params) {
    try {
        var data = JSON.parse(params);
        var apiKey = data.apiKey;
        var prompt = data.prompt;
        var operation = data.operation;
        var strength = data.strength || 0.7;

        // This is a placeholder for the actual API call
        // In a real implementation, you would use the Flux Kontext API here
        // For example, using a server-side proxy to handle the API key securely
        
        // Example response structure (replace with actual API call)
        var response = {
            success: true,
            imageUrl: "https://example.com/generated-image.png"
        };
        
        return JSON.stringify(response);
    } catch (e) {
        return JSON.stringify({ error: e.toString() });
    }
}

// Create a new document with the generated image
function createNewDocument(params) {
    try {
        var data = JSON.parse(params);
        var imageUrl = data.imageUrl;
        
        // This would download the image and create a new document
        // For now, we'll just show an alert
        alert("New document created with generated image: " + imageUrl);
        
        return JSON.stringify({ success: true });
    } catch (e) {
        return JSON.stringify({ error: e.toString() });
    }
}

// Apply the edited image to the current document
function applyEditedImage(params) {
    try {
        var data = JSON.parse(params);
        var imageUrl = data.imageUrl;
        
        // This would download the image and apply it as a new layer
        // For now, we'll just show an alert
        alert("Edited image applied: " + imageUrl);
        
        return JSON.stringify({ success: true });
    } catch (e) {
        return JSON.stringify({ error: e.toString() });
    }
}

// Function to handle evalScript calls from the UI
function execute() {
    var args = [];
    for (var i = 0; i < arguments.length; i++) {
        args.push(arguments[i]);
    }
    
    var func = args.shift();
    if (typeof window[func] === 'function') {
        return window[func].apply(this, args);
    } else {
        return JSON.stringify({ error: 'Function ' + func + ' not found' });
    }
}
