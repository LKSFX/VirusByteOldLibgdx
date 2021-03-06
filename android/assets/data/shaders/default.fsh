varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main(void) {
	gl_FragColor = texture2D(u_texture , v_texCoords) * v_color;
}
