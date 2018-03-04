
/*
 * Copyright 2018 Kostas Anagnostou. All rights reserved.
 * License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
 */

#include "bgfx_compute.sh"

IMAGE2D_RO(s_texOcclusionDepthIn, r32f, 0);
IMAGE2D_WR(s_texOcclusionDepthOut, r32f, 1);

uniform vec4 u_inputRTSize;

NUM_THREADS(16, 16, 1)
void main()
{
	//this shader can be used to both copy a mip over to the output and downscale it. 
	
	ivec2 coord = gl_GlobalInvocationID.xy;
		
	if (all(coord.xy < u_inputRTSize.xy))
	{	
		float maxDepth = 1.0;
		
		if ( u_inputRTSize.z > 1)
		{
			vec4 depths = vec4( imageLoad(s_texOcclusionDepthIn, u_inputRTSize.zw * coord.xy ).r,
								imageLoad(s_texOcclusionDepthIn, u_inputRTSize.zw * coord.xy + ivec2(1,0) ).r,
								imageLoad(s_texOcclusionDepthIn, u_inputRTSize.zw * coord.xy + ivec2(0,1)).r,
								imageLoad(s_texOcclusionDepthIn, u_inputRTSize.zw * coord.xy + ivec2(1,1)).r
								);

			//find and return max depth
			maxDepth = max(max(depths.x, depths.y), max(depths.z, depths.w));
		}
		else
		{
			//do not downscale, just copy the value over to the output rendertarget
			maxDepth = imageLoad(s_texOcclusionDepthIn, coord.xy ).r;
		}
			
		imageStore(s_texOcclusionDepthOut, coord, vec4(maxDepth,0,0,1) );
	}
}