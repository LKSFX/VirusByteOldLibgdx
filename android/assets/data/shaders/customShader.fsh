varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

//Our screen resolution, set from java whenever the display is resized
uniform vec2 u_resolution;

//RADIUS of our vignette, where 0.5 results in a circle fitting the screen
const float RADIUS = 0.75;

//Softness of our vignette, between 0.0 and 1.0
const float SOFTNESS = 0.45;

//Sepia colour, adjust to taste
const vec3 SEPIA = vec3(1.2, 1.0, 0.8);

void main(void) {
	//sample our texture
	vec4 texColor = texture2D(u_texture, v_texCoords);
	
	//1. VIGNETTE
	//determine center position
	vec2 position = (gl_FragCoord.xy / u_resolution.xy) - vec2(0.5);
	position.y *= u_resolution.y / u_resolution.x;
	
	//determine the vector length of the center position
	float len = length(position);
	
	//use smoothstep to create a smooth vignette
	float vignette = smoothstep(RADIUS, RADIUS-SOFTNESS, len);
	
	//apply the vignette with 50% opacity
	texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, 0.5);
	
	//2. GRAYSCALE
	//convert to grayscale using NTSC convertion weights
	float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
	
	//3. SEPIA
	//create our sepia tone from some constant value
	vec3 sepiaColor = vec3(gray) * SEPIA;
	
	//again we'll ue mix so that the sepia effect is at 75%
	texColor.rgb = mix(texColor.rgb, sepiaColor, 0.75);
	
	//final colour, multiplied by vertex colour
	gl_FragColor = texColor * v_color;
	
	//show our length for debugging
	float r = 0.5, softness = 0.05;
	//gl_FragColor = vec4( vec3(gray) * SEPIA, v_color);
}
