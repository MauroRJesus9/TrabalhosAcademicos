using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using TPV2.Data;
using TPV2.Models;

namespace TPV2.Controllers
{
    [Authorize(Roles = "Admin")]
    public class PropertyCategoriesController : Controller
    {
        private readonly ApplicationDbContext _context;

        public PropertyCategoriesController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: PropertyCategories
        public async Task<IActionResult> Index()
        {
            return View(await _context.PropertyCategories.Where(c => c.isRemoved != true).ToListAsync());
        }

        // GET: PropertyCategories/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var propertyCategories = await _context.PropertyCategories
                .FirstOrDefaultAsync(m => m.CategoryId == id);
            if (propertyCategories == null)
            {
                return NotFound();
            }

            return View(propertyCategories);
        }

        // GET: PropertyCategories/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: PropertyCategories/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("CategoryId,Description")] PropertyCategories propertyCategories)
        {
            propertyCategories.isRemoved = false;
            if (ModelState.IsValid)
            {
                _context.Add(propertyCategories);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(propertyCategories);
        }

        // GET: PropertyCategories/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var propertyCategories = await _context.PropertyCategories.FindAsync(id);
            if (propertyCategories == null)
            {
                return NotFound();
            }
            return View(propertyCategories);
        }

        // POST: PropertyCategories/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("CategoryId,Description,isRemoved")] PropertyCategories propertyCategories)
        {
            if (id != propertyCategories.CategoryId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(propertyCategories);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!PropertyCategoriesExists(propertyCategories.CategoryId))
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
            return View(propertyCategories);
        }

        // GET: PropertyCategories/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var propertyCategories = await _context.PropertyCategories
                .FirstOrDefaultAsync(m => m.CategoryId == id);
            if (propertyCategories == null)
            {
                return NotFound();
            }

            return View(propertyCategories);
        }

        // POST: PropertyCategories/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var propertyCategories = await _context.PropertyCategories.FindAsync(id);
            _context.PropertyCategories.Remove(propertyCategories);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool PropertyCategoriesExists(int id)
        {
            return _context.PropertyCategories.Any(e => e.CategoryId == id);
        }
    }
}
