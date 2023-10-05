package cn.ray.gateway.common.constants;

/**
 * @author Ray
 * @date 2023/10/6 06:32
 * @description 网关缓冲区辅助类
 */
public interface GatewayBufferHelper {

    String FLUSHER = "FLUSHER";

    String MPMC = "MPMC";

    static boolean isMpmc(String bufferType) {
        return MPMC.equals(bufferType);
    }

    static boolean isFlusher(String bufferType) {
        return FLUSHER.equals(bufferType);
    }

}
