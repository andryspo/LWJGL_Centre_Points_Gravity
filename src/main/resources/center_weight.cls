inline void AtomicAdd(volatile __global float *source, const float operand) {
    union {
        unsigned int intVal;
        float floatVal;
    } newVal;
    union {
        unsigned int intVal;
        float floatVal;
    } prevVal;
    do {
        prevVal.floatVal = *source;
        newVal.floatVal = prevVal.floatVal + operand;
    } while (atomic_cmpxchg((volatile __global unsigned int *)source, prevVal.intVal, newVal.intVal) != prevVal.intVal);
}

kernel void sum_weight(global float* weight, global float* result, int const size) {

    const int itemId = get_global_id(0);

    if(itemId < size) {
         AtomicAdd(&result[0], weight[itemId]);
         //result[0] += weight[itemId];
    }

}

kernel void center_weight(global const float* x, global const float* y, global float* weight,
                global float* result, float const sum,  int const size) {

    const int itemId = get_global_id(0);
     if(itemId < size) {
         //result[0] += weight[itemId] * x[itemId];
         //result[1] += weight[itemId] * y[itemId];
         AtomicAdd(&result[0], weight[itemId] * x[itemId]);
         AtomicAdd(&result[1], weight[itemId] * y[itemId]);
     }

}

