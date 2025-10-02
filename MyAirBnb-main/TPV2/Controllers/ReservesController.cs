using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using TPV2.Data;
using TPV2.Models;
using Microsoft.AspNetCore.Authorization;

namespace TPV2.Controllers
{

    [Authorize]
    public class ReservesController : Controller
    {
        private readonly ApplicationDbContext _context;

        public ReservesController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: Reserves
        public async Task<IActionResult> Index()
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (User.IsInRole("Admin"))
            {
                var applicationDbContext = _context.Reserves.Include(r => r.Property).Include(r => r.RClientIdReserve).Include(r => r.Status);
                return View(await applicationDbContext.ToListAsync());
            }else if (User.IsInRole("Cliente"))
            {
                var applicationDbContext = _context.Reserves.Include(r => r.Property)
                                            .Include(r => r.RClientIdReserve)
                                            .Include(r => r.Status)
                                            .Where(r => r.ClientId.Contains(userId));
                return View(await applicationDbContext.ToListAsync());
            }else if (User.IsInRole("Gestor"))
            {

                var applicationDbContext = _context.Reserves.Include(r => r.Property)
                                                            .Include(r => r.RClientIdReserve)
                                                            .Include(r => r.Status)
                                                            .Where(r => r.isRemoved != true);

                var propIdUser = (from r in _context.Reserves.Include(r => r.Property).Include(r => r.RClientIdReserve).Include(r => r.Status)
                                  join p in _context.Properties on r.PropertyId equals p.PropertyId
                                 join s in _context.ReserveStatus on r.StatusId equals s.StatusId
                                 join u in _context.AplicationUser on r.ClientId equals u.Id
                                 where p.ProprietarioId == userId
                                 where r.isRemoved != true
                                 select r);

                return View(await propIdUser.ToListAsync());
            }
            else
            {
                var applicationDbContext = _context.Reserves.Include(r => r.Property).Include(r => r.RClientIdReserve).Include(r => r.Status).Where(r => r.isRemoved != true);
                return View(await applicationDbContext.ToListAsync());
            }
                
        }

        // GET: Reserves/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserves = await _context.Reserves
                .Include(r => r.Property)
                .Include(r => r.RClientIdReserve)
                .Include(r => r.Status)
                .FirstOrDefaultAsync(m => m.ReserveId == id);
            if (reserves == null)
            {
                return NotFound();
            }

            return View(reserves);
        }

        [Authorize(Roles = "Cliente")]
        // GET: Reserves/Create
        public IActionResult Create(int? id)
        {
            
            ViewData["PropertyId"] = new SelectList(_context.Properties.Where(p => p.PropertyId == id), "PropertyId", "Address");
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
            ViewData["StatusId"] = new SelectList(_context.ReserveStatus, "StatusId", "Description");
            return View();
        }

        // POST: Reserves/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ReserveId,CheckIn,CheckOut,PropertyId,Observations")] Reserves reserves)
        {
            reserves.isRemoved = false;
            reserves.ClientId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            reserves.StatusId = 3;
            var verificadisponibi = (from reserve in _context.Reserves
                                    where reserve.CheckOut > reserves.CheckIn
                                    where reserve.PropertyId == reserves.PropertyId
                                    select reserve);


            var data = (_context.Reserves
                            .Where(i => i.PropertyId == reserves.PropertyId)
                            .OrderByDescending(i => i.CheckOut)
                            .FirstOrDefault());
            

            if (ModelState.IsValid)
            {
                if (DateTime.Compare(reserves.CheckIn, reserves.CheckOut) < 0) //Verifica se o checkIn é inferior ao checkOut (Datas)
                {
                    if (!verificadisponibi.Any()) //Se não estiver ninguém na propriedade naquele momento de check in adiciona
                    {
                        _context.Add(reserves);
                        await _context.SaveChangesAsync();
                        return RedirectToAction(nameof(Index));
                    }
                    else
                    {
                        TempData["msg"] = $"<script>alert('Escolha outra data.Após dia {data.CheckOut}');</script>";
                    }
                }
            }
            ViewData["PropertyId"] = new SelectList(_context.Properties.Where(p => p.PropertyId == reserves.PropertyId), "PropertyId", "Address", reserves.PropertyId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "Id", reserves.ClientId);
            ViewData["StatusId"] = new SelectList(_context.ReserveStatus, "StatusId", "Description", reserves.StatusId);
            return View(reserves);
        }

        [Authorize(Roles = "Admin, Gestor, Funcionario")]
        // GET: Reserves/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserves = await _context.Reserves.FindAsync(id);
            if (reserves == null)
            {
                return NotFound();
            }
            ViewData["PropertyId"] = new SelectList(_context.Properties, "PropertyId", "Address", reserves.PropertyId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", reserves.ClientId);
            ViewData["StatusId"] = new SelectList(_context.ReserveStatus, "StatusId", "Description", reserves.StatusId);
            return View(reserves);
        }

        // POST: Reserves/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ReserveId,CheckIn,CheckOut,PropertyId,Observations,StatusId,ClientId,isRemoved")] Reserves reserves)
        {
            if (id != reserves.ReserveId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(reserves);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!ReservesExists(reserves.ReserveId))
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
            ViewData["PropertyId"] = new SelectList(_context.Properties, "PropertyId", "Address", reserves.PropertyId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "Id", reserves.ClientId);
            ViewData["StatusId"] = new SelectList(_context.ReserveStatus, "StatusId", "Description", reserves.StatusId);
            return View(reserves);
        }
        [Authorize(Roles = "Admin")]
        // GET: Reserves/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserves = await _context.Reserves
                .Include(r => r.Property)
                .Include(r => r.RClientIdReserve)
                .Include(r => r.Status)
                .FirstOrDefaultAsync(m => m.ReserveId == id);
            if (reserves == null)
            {
                return NotFound();
            }

            return View(reserves);
        }

        // POST: Reserves/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var reserves = await _context.Reserves.FindAsync(id);
            _context.Reserves.Remove(reserves);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool ReservesExists(int id)
        {
            return _context.Reserves.Any(e => e.ReserveId == id);
        }
    }
}
