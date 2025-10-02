using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.SqlServer;
using Microsoft.Extensions.FileProviders;
using TPV2.Data;
using TPV2.Models;

namespace TPV2.Controllers
{
    [AllowAnonymous]
    public class PropertiesController : Controller
    {
        private readonly ApplicationDbContext _context;
        private readonly UserManager<AplicationUser> userManager;

        public PropertiesController(ApplicationDbContext context, UserManager<AplicationUser> userManager)
        {
            _context = context;
            this.userManager = userManager;
        }

        // GET: Properties
        public async Task<IActionResult> Index(int? categoryId)
        {
            var user = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (User.IsInRole("Admin"))
            {
                if(categoryId != null)
                {
                    var applicationDbContext = _context.Properties
                                        .Include(p => p.CProperty)
                                        .Include(p => p.PFuncionarioID)
                                        .Include(p => p.PProprietarioID)
                                        .Where(p => p.CategoryId == categoryId);
                    return View(await applicationDbContext.ToListAsync());
                }
                else
                {
                    var applicationDbContext = _context.Properties.Include(p => p.CProperty).Include(p => p.PFuncionarioID).Include(p => p.PProprietarioID);
                    return View(await applicationDbContext.ToListAsync());
                }
            }
            else if (User.IsInRole("Gestor"))
            {
                if(categoryId != null)
                {
                    var applicationDbContext = _context.Properties.Include(p => p.CProperty)
                                                    .Include(p => p.PFuncionarioID)
                                                    .Include(p => p.PProprietarioID)
                                                    .Where(p => p.isRemoved != true)
                                                    .Where(p => p.ProprietarioId.Contains(user))
                                                    .Where(p => p.CategoryId == categoryId);
                    return View(await applicationDbContext.ToListAsync());
                }
                else
                {
                    var applicationDbContext = _context.Properties.Include(p => p.CProperty)
                                                    .Include(p => p.PFuncionarioID)
                                                    .Include(p => p.PProprietarioID)
                                                    .Where(p => p.isRemoved != true)
                                                    .Where(p => p.ProprietarioId.Contains(user));
                    return View(await applicationDbContext.ToListAsync());
                }
                
            }
            else
            {
                if(categoryId != null)
                {
                    var applicationDbContext = _context.Properties.Include(p => p.CProperty)
                                                    .Include(p => p.PFuncionarioID)
                                                    .Include(p => p.PProprietarioID)
                                                    .Where(p => p.isRemoved != true)
                                                    .Where(p => p.CategoryId == categoryId);
                    return View(await applicationDbContext.ToListAsync());
                }
                else
                {
                    var applicationDbContext = _context.Properties.Include(p => p.CProperty)
                                                    .Include(p => p.PFuncionarioID)
                                                    .Include(p => p.PProprietarioID)
                                                    .Where(p => p.isRemoved != true);
                    return View(await applicationDbContext.ToListAsync());
                }
                
            }
                
        }

        // GET: Properties/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var properties = await _context.Properties
                .Include(p => p.CProperty)
                .Include(p => p.PFuncionarioID)
                .Include(p => p.PProprietarioID)
                .FirstOrDefaultAsync(m => m.PropertyId == id);

            var propertiesImages = _context.PropertiesImages
                                    .Include(p => p.PropertyImage)
                                    .Where(p => p.PropertyID == id);

            var model = new List<ModelImages>();
            foreach(var image in _context.PropertiesImages)
            {
                if(image.PropertyID == id)
                {
                    model.Add(new ModelImages { ImageFile = image.FileName });
                }
            }

            ModelFinalDetailsProperties final = new ModelFinalDetailsProperties();
            final.modelImages = model;
            final.properties = properties;

            if (properties == null)
            {
                return NotFound();
            }

            return View(final);
        }

        [Authorize(Roles = "Gestor")]
        // GET: Properties/Create
        public IActionResult Create()
        {
            //var user = User.FindFirstValue(ClaimTypes.NameIdentifier);
            ViewData["CategoryId"] = new SelectList(_context.PropertyCategories, "CategoryId", "Description");
            //ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser.Where(userManager.IsInRoleAsync(p => p.Id,"Funcionario")), "Id", "UserName");
            /*var query = $"SELECT Users.Id AS Id, Users.UserName AS UserName
                        FROM dbo.AspNetUsers AS Users
                        JOIN dbo.AspNetUsersRoles ON Users.Id equals dbo.AspNetUsersRoles.UserId
                        JOIN dbo.AspNetRoles ON dbo.AspNetUsersRoles.RoleId equals dbo.AspNetRoles.Id
                        WHERE dbo.AspNetRoles = 'Funcionario'";*/


            //ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
            var users = (from user in _context.Users
                              join userRole in _context.UserRoles
                              on user.Id equals userRole.UserId
                              join role in _context.Roles
                              on userRole.RoleId equals role.Id
                              where role.Name == "Funcionario"
                              select user);

            ViewData["FuncionarioId"] = new SelectList(users, "Id", "UserName");

            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
            return View();
        }

        /*public static IQueryable<IdentityUser> GetUsersInRole(ApplicationDbContext db, string roleName)
        {
            return from user in db.Users
                   where user.Roles.Any(r => r.Role.Name == roleName)
                   select user;
        }*/


        // POST: Properties/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [Authorize(Roles = "Gestor")]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Name,Description,Price,Address,FuncionarioId,CategoryId")] Properties properties)
        {
            var user = User.FindFirstValue(ClaimTypes.NameIdentifier);
            properties.isRemoved = false;
            properties.ProprietarioId = user;
            if (ModelState.IsValid)
            {
                _context.Add(properties);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["CategoryId"] = new SelectList(_context.PropertyCategories, "CategoryId", "Description", properties.CategoryId);
            ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", properties.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", properties.ProprietarioId);
            return View(properties);
        }

        // GET: Properties/Edit/5
        [Authorize(Roles = "Gestor")]
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var properties = await _context.Properties.FindAsync(id);
            if (properties == null)
            {
                return NotFound();
            }
            ViewData["CategoryId"] = new SelectList(_context.PropertyCategories, "CategoryId", "Description", properties.CategoryId);

            var users = (from user in _context.Users
                         join userRole in _context.UserRoles
                         on user.Id equals userRole.UserId
                         join role in _context.Roles
                         on userRole.RoleId equals role.Id
                         where role.Name == "Funcionario"
                         select user);

            ViewData["FuncionarioId"] = new SelectList(users, "Id", "UserName", properties.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName", properties.ProprietarioId);
            return View(properties);
        }

        // POST: Properties/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [Authorize(Roles = "Gestor")]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("PropertyId,Name,Description,Price,Address,FuncionarioId,CategoryId,isRemoved")] Properties properties, List<IFormFile> files)
        {
            var user = User.FindFirstValue(ClaimTypes.NameIdentifier);
            properties.ProprietarioId = user;
            PropertiesImages propertiesImages;
            if (id != properties.PropertyId)
            {
                return NotFound();
            }

            //Adicionar imagens
            if (files != null)
            {
                foreach (var file in files)
                {
                    if (file.Length > 0)
                    {
                        //Getting FileName
                        var fileName = Path.GetFileName(file.FileName);

                        //Assigning property id to name
                        var myUniqueFileName = $@"{Guid.NewGuid()}";

                        //Getting file Extension
                        var fileExtension = Path.GetExtension(fileName);

                        // concatenating  FileName + FileExtension
                        var newFileName = String.Concat(myUniqueFileName, fileExtension);

                        // Combines two strings into a path.
                        var filepath =
                new PhysicalFileProvider(Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "PropertyPics")).Root + $@"\{newFileName}";

                        using (FileStream fs = System.IO.File.Create(filepath))
                        {
                            file.CopyTo(fs);
                            fs.Flush();
                        }
                        //Adicionar à tabela PropertiesImages
                        propertiesImages = new PropertiesImages();
                        propertiesImages.FileName = newFileName;
                        propertiesImages.PropertyID = id;
                        _context.Add(propertiesImages);
                        await _context.SaveChangesAsync();
                    }
                }
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(properties);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!PropertiesExists(properties.PropertyId))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["CategoryId"] = new SelectList(_context.PropertyCategories, "CategoryId", "Description", properties.CategoryId);
            ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", properties.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", properties.ProprietarioId);
            return View(properties);
        }
        [Authorize(Roles = "Admin")]
        // GET: Properties/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var properties = await _context.Properties
                .Include(p => p.CProperty)
                .Include(p => p.PFuncionarioID)
                .Include(p => p.PProprietarioID)
                .FirstOrDefaultAsync(m => m.PropertyId == id);
            if (properties == null)
            {
                return NotFound();
            }

            return View(properties);
        }

        // POST: Properties/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var properties = await _context.Properties.FindAsync(id);
            _context.Properties.Remove(properties);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool PropertiesExists(int id)
        {
            return _context.Properties.Any(e => e.PropertyId == id);
        }
    }
}
