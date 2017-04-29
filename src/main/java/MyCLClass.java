import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opencl.CL10.CL_PROGRAM_BUILD_LOG;

public class MyCLClass {

    private CLContext context;
    private CLPlatform platform;
    private List<CLDevice> devices;
    private CLCommandQueue queue;

    private final int size;
    private final PointUtil pointUtil;


    MyCLClass(int size, PointUtil pointUtil) {

        this.pointUtil = pointUtil;
        this.size = size;
    }

    public void center_weight() throws LWJGLException {
        // Create our OpenCL context to run commands
        initializeCL();
        // Create an OpenCL 'program' from a source code file
        CLProgram sumProgram = CL10.clCreateProgramWithSource(context, new FileLoader().loadText("center_weight"), null);
        // Build the OpenCL program, store it on the specified device
        int error = CL10.clBuildProgram(sumProgram, devices.get(0), "", null);
        // Check for any OpenCL errors
        if (sumProgram.getBuildInfoString(devices.get(0), CL_PROGRAM_BUILD_LOG) != null) {
            System.out.println(sumProgram.getBuildInfoString(devices.get(0), CL_PROGRAM_BUILD_LOG));
        }
        Util.checkCLError(error);

        CLKernel sumKernel;
        CLKernel center_kernel;

        final float[] x, y, weight, res, sumWeight;
        final FloatBuffer xBuff, yBuff, weightBuff, sumBuff, resultBuff;
        final CLMem xMemory, yMemory, weightSumMemory, weightMemory, resultMemory;
        final int dimensions = 1;
        long startTime, endTime, dtime;
        // Create a kernel instance of our OpenCl program

        sumKernel = CL10.clCreateKernel(sumProgram, "sum_weight", null);
        center_kernel = CL10.clCreateKernel(sumProgram, "center_weight", null);

        // Error buffer used to check for OpenCL error that occurred while a command was running
        IntBuffer errorBuff = BufferUtils.createIntBuffer(1);

        x = pointUtil.getX();
        y = pointUtil.getY();
        weight = pointUtil.getWeight();
        // Create a buffer containing our array of numbers, we can use the buffer to create an OpenCL memory object


        xBuff = BufferUtils.createFloatBuffer(x.length);
        xBuff.put(x);
        xBuff.rewind();
        // Create an OpenCL memory object containing a copy of the data buffer
        xMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, xBuff, errorBuff);
        // Check if the error buffer now contains an error
        Util.checkCLError(errorBuff.get(0));

        // Create our second array of numbers
        // Create a buffer containing our second array of numbers
        yBuff = BufferUtils.createFloatBuffer(y.length);
        yBuff.put(y);
        yBuff.rewind();

        // Create an OpenCL memory object containing a copy of the data buffer
        yMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, yBuff, errorBuff);
        // Check if the error buffer now contains an error

        weightBuff = BufferUtils.createFloatBuffer(weight.length);
        weightBuff.put(weight);
        weightBuff.rewind();
        weightMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, weightBuff, errorBuff);

        Util.checkCLError(errorBuff.get(0));

        // Create an empty OpenCL buffer to store the result of adding the numbers together
        weightSumMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, 8, errorBuff);
        // Check for any error creating the memory buffer
        Util.checkCLError(errorBuff.get(0));

        // Set the kernel parameters

        sumKernel.setArg(0, weightMemory);
        sumKernel.setArg(1, weightSumMemory);
        sumKernel.setArg(2, size);
        // Create a buffer of pointers defining the multi-dimensional size of the number of work units to execute

        PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
        globalWorkSize.put(0, size);

        // Run the specified number of work units using our OpenCL program kernel

        startTime = System.currentTimeMillis();
        CL10.clEnqueueNDRangeKernel(queue, sumKernel, dimensions, null, globalWorkSize, null, null, null);

        //  CL10.clFinish(queue);
        endTime = System.currentTimeMillis();
        dtime = endTime - startTime;
        //This reads the result memory buffer
        sumBuff = BufferUtils.createFloatBuffer(1);
        // We read the buffer in blocking mode so that when the method returns we know that the result buffer is full
        sumWeight = new float[1];

        CL10.clEnqueueReadBuffer(queue, weightSumMemory, CL10.CL_TRUE, 0, sumBuff, null, null);

        // Print the values in the result buffer
        sumBuff.get(sumWeight);
        resultMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, 8, errorBuff);

        resultBuff = BufferUtils.createFloatBuffer(2);
        center_kernel.setArg(0, xMemory);
        center_kernel.setArg(1, yMemory);
        center_kernel.setArg(2, weightMemory);
        center_kernel.setArg(3, resultMemory);
        center_kernel.setArg(4, sumWeight[0]);
        center_kernel.setArg(5, size);

        startTime = System.currentTimeMillis();
        CL10.clEnqueueNDRangeKernel(queue, center_kernel, dimensions, null, globalWorkSize, null, null, null);
        CL10.clFinish(queue);
        endTime = System.currentTimeMillis();
        dtime += endTime - startTime;
        res = new float[2];

        CL10.clEnqueueReadBuffer(queue, resultMemory, CL10.CL_TRUE, 0, resultBuff, null, null);

        resultBuff.get(res);


        System.out.println("centre of gravity:");
        System.out.println("x: " + res[0] / sumWeight[0]);
        System.out.println("y: " + res[1] / sumWeight[0]);

        System.out.println("working time = " + dtime + " milliseconds");

        // Destroy our kernel and program
        CL10.clReleaseKernel(sumKernel);
        CL10.clReleaseProgram(sumProgram);
        // Destroy our memory objects
        CL10.clReleaseMemObject(xMemory);
        CL10.clReleaseMemObject(yMemory);
        CL10.clReleaseMemObject(weightSumMemory);
        // Destroy the OpenCL context
        destroyCL();
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

    public long getDeviceInfo(final int param) throws LWJGLException {
        // Create our OpenCL context to run commands
        initializeCL();

        return devices.get(0).getInfoInt(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES);
    }
}
