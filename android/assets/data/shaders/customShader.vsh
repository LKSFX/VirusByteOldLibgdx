attribute vec4 a_color;
attribute vec3 a_position; // incoming position attribute from our SpriteBatch
attribute vec2 a_texCoord0;

// the transformation matrix of our SpriteBatch
uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

void main(void) {
	v_color = a_color;
	v_texCoords = a_texCoord0;
	gl_Position = u_projTrans * vec4(a_position, 1.0);
}
