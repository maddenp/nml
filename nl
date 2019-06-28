 &QUEUEnamelist
   ComputeTasks = '240' ! Number of compute tasks for FIM (1 for serial) 
   MaxQueueTime = '04:00:00'                        ! Run time for complete job (HH:MM:SS) [ Ignored by WFM ]
   PREPDIR = 'nodir'                                ! If exists, use for prep otherwise calculate prep
   FIMDIR  = 'nodir'                                ! If exists, use for FIM otherwise calculate FIM
   DATADIR = '/global/lustre/users/spire/shared/data/fim/input/static'  ! Location of gfsltln and global_mtnvar files
   DATADR2 = '/global/lustre/users/spire/shared/data/ncep'  ! Location of the sanl file and the sfcanl file
   DATADR3 = '/global/lustre/users/spire/shared/data/ecmwf'  ! Location of the ECMWF ICs
   chem_datadir = '/scratch4/BMC/fim/fimdata'       ! Location of chemistry data files
/
 &TOPOnamelist
   topodatfile = '/global/lustre/users/spire/thender/FIMDATA/DATADIR/wrf5mintopo.dat'
/
 &LANDnamelist
!!   landsmoothfact=0.75
   landdatdir='/global/lustre/users/spire/thender/FIMDATA/DATADIR/'  ! Need ending / in path name!!
   landdatfile='geo_em.d01.nc'
   landglvldir='./'
   niland=4001
   njland=2000
/
 &CNTLnamelist
   glvl                = 8            ! Grid level
   SubdivNum           = 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 ! Subdivision numbers for each recursive refinement(2: bisection, 3: trisection, etc.)
   nvl                 = 64           ! Number of atmospheric layers 
/
 &PREPnamelist
   NumPostGrids        = 1                          ! Number of post-processing grids
   PostGridIds         = 193                        ! Post-processing grid ids
   curve               = 0                          ! 0: ij order, 1: Hilbert curve order (only for all-bisection refinement), 2:ij block order, 3: Square Layout
   gtype               = 2                          ! grid type: 0-standard recursive,2-modified recursive,3-modified nonrecursive
   NumCacheBlocksPerPE = 1                          ! Number of cache blocks per processor. Only applies to ij block order
   alt_topo            = .true.                     ! if true, use alternate srf.height field
   alt_land            = .true.                     ! if true, use MODIS data
   aerosol_file      = 'climaeropac_global.txt'     ! filename relative to DATADIR
   gfsltln_file      = 'no_such_file'               ! Correct value will be set by run automation
   mtnvar_file       = 'no_such_file'               ! Correct value will be set by run automation
/
 &DIAGnamelist
   PrintIpnDiag      = -1             ! ipn at which to print diagnostics (-1 means no print)
   PrintDiagProgVars = 12             ! Hourly increment to print diagnostic prognosis variables (-1=>none, 0=>every step)
   PrintDiagNoise    =  1             ! Hourly increment to print diagnostic gravity wave noise  (-1=>none, 0=>every step)
   PrintDiags        = .false.        ! True means print diagnostic messages
/
 &MODELnamelist
   nts               =  0             ! number of time steps
   UpdateSST         = .false.        ! True means update sst and sea ice by reading in a file during integration
   rleigh            = 2.0            ! rayleigh damping time scale (days^-1)
   rlthresh          = 90.            ! min speed (m/s) for rayleigh damping
   rltaper           = 0.4            ! tapering coeff for rayleigh damping
   veldff_bkgnd      = 1.             ! diffusion velocity (=diffusion/mesh size)
   veldff_boost      = 3.             ! veldff at model top (linear ramp-up over several layers)
   biharm_frst       = 0              ! biharmonic dissip. starts in this layer
   biharm_last       = 0              ! biharmonic dissip. ends in this layer
   thktop            = 50.            ! min.thknss (Pa) of uppermost model layer
   intfc_smooth      = 50.            ! [diffusivity/mesh size] (m/s) for intfc smoothing (0 = no smoothing)
   slak              = 0.5            ! intfc movement retardation coeff (1 = no retardation)
   miglim            = 1	      ! intfc migration limit (< 0: no limit)
   remap_optn        = 2              ! vert.advec option: 1-PCM; 2-PLM; 3-PPM
   pure_sig          = .false.        ! if true, use GFS sigma-p coordinate
   digifilt          = .false.        ! DFI - digital filter initalization
   tfiltwin          = 5400           ! 1/2 length duration of DFI (s)
   wts_type          = 3              ! type of digital filter (1=Lanczos,2=Hamming,3=Dolph)
   addtend           = .false.
   smoothtend        = .false.
   flux_conserv_schm = 2
   hiOrderFluxComp   = .true.
   hiOrderUVcomp     = .false.
   IC_fmt            = 'nemsio'       ! Format of IC files (sigio or nemsio)
   IC_atm            = 'nemsio'       ! Format of IC atmospheric files if IC_fmt/='sigio' (nemsio or ecmwf)
/
 &PHYSICSnamelist
   PhysicsInterval   =  180           ! Interval in seconds to call non-radiation physics (0=every step)
   RadiationInterval =  3600          ! Interval in seconds to call radiation physics (0=every step)
   SSTInterval       =  86400         ! Interval in seconds to call update_sst (0=every step)
   num_p3d           =  4             ! 4 means call Zhao/Carr/Sundqvist Microphysics
   ishal_cnv         = 1	      ! shallow conv: 0=none; 1=SAS; 2=GF; 3=SAS&GF
   ideep_cnv         = 1	      ! deep    conv: 0=none; 1=SAS; 2=GF;
/
 &gfsphys
   ictm = 1 
! jbao new gfs 2014 physics intialize radiation
!!      isol=2   ! 
   isol = 0   ! 
   ico2 = 2 
   iaer = 111 
   iaer_mdl = 0 
   ialb = 0 
!        iems=0 
!  gfs parallel runs Eveyln made has iems=1
   iems = 1 
   iovr_sw = 1 
   iovr_lw = 1 
   isubc_sw = 2 
   isubc_lw = 2 
   n3dflxtvd  = 3
   n2dzhaocld = 2
   n3dzhaocld = 4
   n2dcldpdf=   0  
   n3dcldpdf=   0   ! from gfs 2014 input
   fhswr = 3600.  ! jbao 
   fhlwr = 3600.  ! jbao 
/

 &TIMEnamelist
  yyyymmddhhmm = "201809151200"       ! date of the model run
/
 &OUTPUTnamelist
   ArchvTimeUnit     =  'hr'          ! ts:timestep; mi:minute; hr:hour; dy:day; mo:month
   TotalTime         =  168           ! Total integration time in ArchvTimeUnit
   ArchvIntvl        =  6             ! Archive interval in ArchvTimeUnit
   readrestart       = .false.         ! True means start by reading restart file (rpointer)
   restart_freq      =  24            ! Restart interval in ArchvTimeUnit
   PrintMAXMINtimes  = .true.         ! True means print MAX MIN routine times, false means print for each PE
   preexchangebarrier  = .false.       ! True means enable timed barrier before exchange
   postexchangebarrier = .false.       ! True means enable timed barrier after exchange
   FixedGridOrder    = .true.        ! True: always output in the same order(IJ), False: order determined by curve. Does not apply to IJorder
/
 &ISOBARICnamelist
   isobaric_levels_file = "output_isobaric_levs.txt" ! file containing pressure levels, in FIMrun directory
 /

!
! WRITETASKnamelist is used to optionally create a separate group of
! FIM-specific write tasks to speed up FIM model output by overlapping disk
! writes with computation. By default this feature is turned off. When enabled,
! write tasks intercept FIM output and write to disk while the main compute
! tasks continue with model computation. In NEMS lingo, write tasks are called
! "quilt" tasks.
!
! WRITETASKnamelist is ignored for a serial run.
!
 &WRITETASKnamelist
   check_omp_consistency = .true.     ! FIM checks for consistency of OpenMP settings
   abort_on_bad_task_distrib = .true. ! Abort FIM when node names are not as expected
   cpn = 16      ! Number of cores per node 
   mpipn = 16   ! Number of MPI tasks per node 
   nthreads = 1  ! Number of threads per task if OMP threading enabled 
   debugmsg_on = .false.              ! Print verbose debug messages
   max_write_tasks_per_node = 1       ! Maximum number of write tasks to place on a single node
   num_write_tasks = 1                ! Use: 0 = no write tasks, 1 = one
   root_own_node = .true. ! whether root process has node to itself 
/
!
  &POSTnamelist
!
! input and output specifications:
!
  post_datadir = "../fim"
  outputdir = "."
!  input = "/tg2/projects/fim/jlee/PREP/mdrag5h.dat"
  input = ""
!  if input has content, it overwrites the datadir
!  output = "/p72/fim/wang/nc_files/mdrag5h.nc"
  output = ""
!  if output has content, it overwrites the outputdir
  output_fmt = "grib"         ! "nc" --netCDF file, "grib" --GRIB file
  multiple_output_files = .true. !  -- multiple grib outputfiles (assumed true when post in fim)
!
! grid specifications:
!
  gribtable = "fim_gribtable" ! only used by grib output file(s)
  grid_id = 193               ! 228(144, 73), 45(288, 145), 3(360, 181), 4(720, 361),
                              ! 174(2880, 1440), etc.; only for grib output file
  mx = 720                    ! only used by netcdf output file
  my = 360                    ! only used by netcdf output file
  latlonfld = .true.          ! true -- create lat lon field in grib output file
!
! post processing specifications:
!
  is = 1                      ! interpolation scheme:
                              ! 0 -- no interpolation: native grid;
                              ! 1 -- horizontal interpo. on native vertical coord.;
                              ! 2 -- horizontal interpo. + vertical interpo. on std. pressure levels;
                              ! 3 -- horizontal interpo. + vertical interpo. on 10mb inc. pressure levels;
  vres = 111                  ! only used in vertical interpolation
  mode = "linear"             ! step or linear interpolation for vertical column
!
! variable specifications:
!
  var_list='u12D','v12D','t22D','td2D','ts2D','ms2D','rn2D','pw2D','pq2D','hfss','hfls','sn2D','rlut','r42D','s62D','up3P','vp3P','tmpP','hgtP','vv3P','qc3P','rp3P','sp3P','ph3D','tk3D','us3D','vs3D', 'prpv', 'thpv', 'hmws', 'umws', 'vmws', 'hmwU', 'umwU', 'vmwU'
  nsmooth_var=1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,1,1,1,2,1,0,2,0,1,1,1,1,1,1,1,1,1,1,1,1

  fimout = .true.    ! whether to write FIM binary output files
  gribout = .false.  ! whether to write GRIB output files from FIM
  only_write_var_list_entries = .false.
  t1          = -1
  t2          = -1
  delta_t     = -1
  multiHrTu   = .true.
/

&chemwrf
  chem_opt  = 0 ! chem option, 0=off, 300=on
  chemdt    = 30.
  kemit     = 1
  DUST_OPT        =           1,
  DMSEMIS_OPT     =           1,
  SEAS_OPT        =           0,
  BIO_EMISS_OPT   =           0,
  BIOMASS_BURN_OPT        =   1,
  PLUMERISEFIRE_FRQ       =   30,
  EMISS_INPT_OPT  =           1,
  GAS_BC_OPT      =           0,
  GAS_IC_OPT      =           0,
  AER_BC_OPT      =           0,
  AER_IC_OPT      =           0,
/

&wrfphysics
  mp_physics                = 0,    ! 0=off, 2=on
  cu_physics                = 0,    ! 0=off, 3=on
/

!
! System specific parameters for MPI, task geometry, etc.
!
 &SYSTEMnamelist
  MPIRUNCMD='mpirun -n'
/
