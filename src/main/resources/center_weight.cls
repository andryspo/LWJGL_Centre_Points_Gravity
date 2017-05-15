kernel void center_weight(global float* weight, global const float* x, global const float* y,
    global float* result_sum, global float* resultX, global float* resultY,
    int threads_count, int const size) {

    const int itemId = get_global_id(0);

    if(threads_count < size) {
        if(itemId < size) {

            int ost = size % threads_count;
            int part_size = size / threads_count;

            if(itemId == 0 && ost != 0) {
                int n = part_size + ost;
                for(int i = 0; i < n; i++) {
                    resultX[itemId] += x[i] * weight[i];
                    resultY[itemId] += y[i] * weight[i];
                    result_sum[itemId] += weight[i];
                }
            } else {
                int n = part_size;
                for(int i = itemId * n + ost;  i < n * (itemId + 1) + ost; i++ ) {
                    result_sum[itemId] += weight[i];
                    resultX[itemId] += x[i] * weight[i];
                    resultY[itemId] += y[i] * weight[i];

                }
            }
        }
    } else if(itemId < size) {
        result_sum[itemId] += weight[itemId];
        resultX[itemId] += x[itemId] * weight[itemId];
        resultY[itemId] += y[itemId] * weight[itemId];
    }
}
