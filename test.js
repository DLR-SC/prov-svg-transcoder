const AWS = require('aws-sdk'); 

const lambda = new AWS.Lambda({region: 'eu-central-1', apiVersion: '2015-03-31'});
let params = { 
		FunctionName : 'image-converter-dev-svgs2binary', 
		InvocationType : 'RequestResponse', 
		LogType : 'None', 
		Payload: JSON.stringify({ binaryType: 'jpg', body: ['<svg height="150" width="500" xmlns="http://www.w3.org/2000/svg"><ellipse cx="240" cy="100" rx="220" ry="30" style="fill:purple" /><ellipse cx="220" cy="70" rx="190" ry="20" style="fill:lime" /><ellipse cx="210" cy="45" rx="170" ry="15" style="fill:yellow" /></svg>', '<svg height="150" width="500" xmlns="http://www.w3.org/2000/svg"><ellipse cx="240" cy="100" rx="220" ry="30" style="fill:purple" /><ellipse cx="220" cy="70" rx="190" ry="20" style="fill:lime" /><ellipse cx="210" cy="45" rx="170" ry="15" style="fill:yellow" /></svg>']})
};

let params2 = { 
		FunctionName : 'image-converter-dev-svg2binary', 
		InvocationType : 'RequestResponse', 
		LogType : 'None', 
		Payload: JSON.stringify({ binaryType: 'svg', body: '<svg height="150" width="500" xmlns="http://www.w3.org/2000/svg"><ellipse cx="240" cy="100" rx="220" ry="30" style="fill:purple" /><ellipse cx="220" cy="70" rx="190" ry="20" style="fill:lime" /><ellipse cx="210" cy="45" rx="170" ry="15" style="fill:yellow" /></svg>'})
};

lambda.invoke(params, function(err, data) {
    if (err) {
       console.error(err);
    } else {
    	let payload = JSON.parse(data.Payload);
    	let payload2 = JSON.parse(payload.body);
       console.log(payload2.data.key);
    }
 });	