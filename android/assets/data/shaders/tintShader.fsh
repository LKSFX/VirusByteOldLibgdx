#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main(void) {
	vec4 normalColor = texture2D(u_texture , v_texCoords);
	gl_FragColor =  vec4(normalColor.a, normalColor.a, normalColor.a, normalColor.a) * v_color;
}