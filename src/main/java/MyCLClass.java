import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opencl.CL10.CL_PROGRAM_BUILD_LOG;

public class MyCLClass {

    private CLContext context;
    private CLPlatform platform;
    private List<CLDevice> devices;
    private CLCommandQueue queue;

    private final int size;
    private final PointUtil pointUtil;
    private final int threadsCount;

    private long dtime;

    MyCLClass(int size, PointUtil pointUtil, int threadsCount) {
        this.threadsCount = threadsCount;
        this.pointUtil = pointUtil;
        this.size = size;
    }

    public void center_weight() throws LWJGLException {
        // Create our OpenCL context to run commands
        initializeCL();
        // Create an OpenCL 'program' from a source code file
        CLProgram program = CL10.clCreateProgramWithSource(context, new FileLoader().loadText("center_weight"), null);
        final int dimensions = 1;
        // Build the OpenCL program, store it on the specified device
        int error = CL10.clBuildProgram(program, devices.get(0), "", null);
        // Check for any OpenCL errors
        if (program.getBuildInfoString(devices.get(0), CL_PROGRAM_BUILD_LOG) != null) {
            System.out.println(program.getBuildInfoString(devices.get(0), CL_PROGRAM_BUILD_LOG));
        }
        Util.checkCLError(error);
        PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
        globalWorkSize.put(0, threadsCount);
        pointsCalc(program, dimensions, globalWorkSize);

    }

    public float[] pointsCalc(CLProgram program, int dimensions, PointerBuffer globalWorkSize) {

        CLKernel center_kernel;
        IntBuffer errorBuff = BufferUtils.createIntBuffer(1);

        long endTime, startTime;
        final float[] x, y, weight, res;
        final FloatBuffer xBuff, yBuff, weightBuff, resultXBuff, resultYBuff, resultSumBuf;
        final CLMem xMemory, yMemory, weightMemory, resultX, resultY, resultSum;

        center_kernel = CL10.clCreateKernel(program, "center_weight", null);

        x = pointUtil.getX();
        y = pointUtil.getY();

        xBuff = BufferUtils.createFloatBuffer(x.length);
        xBuff.put(x);
        xBuff.rewind();
        xMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, xBuff, errorBuff);

        Util.checkCLError(errorBuff.get(0));

        yBuff = BufferUtils.createFloatBuffer(y.length);
        yBuff.put(y);
        yBuff.rewind();

        weight = pointUtil.getWeight();

        weightBuff = BufferUtils.createFloatBuffer(weight.length);
        weightBuff.put(weight);
        weightBuff.rewind();
        weightMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, weightBuff, errorBuff);

        yMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, yBuff, errorBuff);

        resultY = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, 8 * threadsCount, errorBuff);
        resultX = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, 8 * threadsCount, errorBuff);
        resultSum = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, 8 * threadsCount, errorBuff);

        resultXBuff = BufferUtils.createFloatBuffer(threadsCount);
        resultYBuff = BufferUtils.createFloatBuffer(threadsCount);
        resultSumBuf = BufferUtils.createFloatBuffer(threadsCount);

        center_kernel.setArg(0, weightMemory);
        center_kernel.setArg(1, xMemory);
        center_kernel.setArg(2, yMemory);
        center_kernel.setArg(3, resultSum);
        center_kernel.setArg(4, resultX);
        center_kernel.setArg(5, resultY);
        center_kernel.setArg(6, threadsCount);
        center_kernel.setArg(7, size);

        startTime = System.currentTimeMillis();
        CL10.clEnqueueNDRangeKernel(queue, center_kernel, dimensions, null, globalWorkSize, null, null, null);
        CL10.clFinish(queue);
        endTime = System.currentTimeMillis();
        dtime = endTime - startTime;
        res = new float[3];

        float[] resX = new float[threadsCount];
        float[] resY = new float[threadsCount];
        float[] resSum = new float[threadsCount];

        CL10.clEnqueueReadBuffer(queue, resultX, CL10.CL_TRUE, 0, resultXBuff, null, null);
        CL10.clEnqueueReadBuffer(queue, resultY, CL10.CL_TRUE, 0, resultYBuff, null, null);
        CL10.clEnqueueReadBuffer(queue, resultSum, CL10.CL_TRUE, 0, resultSumBuf, null, null);

        resultXBuff.get(resX);
        resultYBuff.get(resY);
        resultSumBuf.get(resSum);

//        System.out.println(Arrays.toString(resX));
//        System.out.println(Arrays.toString(resY));
//        System.out.println(Arrays.toString(resSum));

        for (int i = 0; i < resX.length; i++) {
            res[0] += resX[i];
            res[1] += resY[i];
            res[2] += resSum[i];
        }
//        System.out.println(res[0] + " " + res[1] + " " + res[2]);
        System.out.println("Center weight: x = " + res[0] / res[2] + " y = " + res[1] / res[2]);
        System.out.println("time: " + dtime + " millis.");

        CL10.clReleaseMemObject(xMemory);
        CL10.clReleaseMemObject(yMemory);

        destroyCL();

        return res;
    }


    // For simplicity exception handling code is in the method calling this one.
    private void initializeCL() throws LWJGLException {
        IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
        CL.create();
        platform = CLPlatform.getPlatforms().get(0);
        devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
        context = CLContext.create(platform, devices, errorBuff);
        queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuff);
        Util.checkCLError(errorBuff.get(0));
    }

    private void destroyCL() {
        // Finish destroying anything we created
        CL10.clReleaseCommandQueue(queue);
        CL10.clReleaseContext(context);
        // And release OpenCL, after this method call we cannot use OpenCL unless we re-initialize it
        CL.destroy();
    }
}
